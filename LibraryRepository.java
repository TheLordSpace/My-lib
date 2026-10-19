import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * الكلاس LibraryRepository (مكتبة الكتب)
 * ------------------------------------------------------------
 * مسؤول عن قراءة الكتب من قاعدة بيانات PostgreSQL.
 * بدل ما يرجّع كل الكتب مرة واحدة (ممكن تكون آلاف الكتب وتبطّأ البرنامج)،
 * بيرجّع الكتب على صفحات، كل صفحة فيها 10 كتب بس.
 *
 * ملحوظة: بيعتمد على جدول books اللي بينشئه كلاس BookRepository.
 */
public class LibraryRepository {

    // عدد الكتب في كل صفحة (ثابت: قيمته 10 ومبتتغيرش)
    public static final int PAGE_SIZE = 10;

    // عنوان الاتصال بقاعدة البيانات
    private final String url;

    // اسم المستخدم بتاع قاعدة البيانات
    private final String user;

    // كلمة سر المستخدم
    private final String password;

    /**
     * دالة الإنشاء: بنبعتلها بيانات الاتصال بالقاعدة مرة واحدة.
     * مثال على الـ url: "jdbc:postgresql://localhost:5432/bookdb"
     */
    public LibraryRepository(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    /**
     * الدالة الأساسية: بترجع كتب صفحة واحدة (10 كتب على الأكثر).
     *
     * @param page رقم الصفحة المطلوبة، بنبدأ العد من 1
     *             (1 = أول 10 كتب، 2 = الكتب من 11 لـ 20، وهكذا)
     * @return قائمة فيها كتب الصفحة دي (فاضية لو الصفحة مفيهاش كتب)
     * @throws SQLException لو حصلت مشكلة في الاتصال أو القراءة
     */
    public List<Book> getAllBooks(int page) throws SQLException {

        // لو المستخدم طلب صفحة رقمها أقل من 1 (صفر أو سالب) نعتبرها الصفحة الأولى
        if (page < 1) {
            page = 1;
        }

        // نحسب نبدأ من أنهي كتاب (OFFSET): بنتخطى كتب الصفحات اللي قبلها.
        // مثال: الصفحة 3 → نتخطى (3 - 1) × 10 = 20 كتاب ونبدأ من الكتاب 21
        int offset = (page - 1) * PAGE_SIZE;

        // أمر SQL:
        // ORDER BY id : بيرتب الكتب بترتيب إضافتها، عشان الصفحات تفضل ثابتة ومتتكررش كتب
        // LIMIT ?     : أقصى عدد كتب نرجعه (10)
        // OFFSET ?    : عدد الكتب اللي هنتخطاها من الأول
        // بنجيب كمان عمود id عشان المستخدم يعرف رقم كل كتاب ويقدر يحذفه
        String sql = "SELECT id, title, author, total_pages, pages_read "
                + "FROM books ORDER BY id LIMIT ? OFFSET ?";

        // القائمة اللي هنجمع فيها الكتب ونرجعها في الآخر
        List<Book> books = new ArrayList<>();

        // try-with-resources: بيقفل الاتصال تلقائيًا لما نخلص
        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            // نحط القيم مكان علامات الاستفهام بالترتيب
            statement.setInt(1, PAGE_SIZE); // LIMIT
            statement.setInt(2, offset);    // OFFSET

            // ننفذ الأمر ونستلم الصفوف
            try (ResultSet resultSet = statement.executeQuery()) {

                // نلف على كل صف راجع من القاعدة (كل صف = كتاب)
                while (resultSet.next()) {
                    // نحول بيانات الصف لكائن Book ونضيفه للقائمة
                    Book book = new Book(
                            resultSet.getString("title"),
                            resultSet.getString("author"),
                            resultSet.getInt("total_pages"),
                            resultSet.getInt("pages_read")
                    );
                    // نحط رقم الكتاب (ID) اللي جه من القاعدة
                    book.setId(resultSet.getInt("id"));
                    books.add(book);
                }
            }
        }

        // نرجع كتب الصفحة المطلوبة
        return books;
    }

    /**
     * دالة مساعدة: بترجع عدد الصفحات الكلي.
     * مفيدة عشان تعرف تعرض للمستخدم "صفحة 2 من 5" وتعرف إمتى توقف.
     * مثال: 23 كتاب → 3 صفحات (10 + 10 + 3)
     *
     * @return عدد الصفحات الكلي (صفر لو مفيش كتب)
     */
    public int getTotalPages() throws SQLException {
        // أمر SQL بيعد كل الكتب في الجدول
        String sql = "SELECT COUNT(*) FROM books";

        try (Connection connection = DriverManager.getConnection(url, user, password);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            if (resultSet.next()) {
                int totalBooks = resultSet.getInt(1);

                // نقسم على 10 مع التقريب لأعلى (ceil) عشان الكتب الباقية تاخد صفحة كمان
                return (int) Math.ceil((double) totalBooks / PAGE_SIZE);
            }
        }

        return 0;
    }

    /**
     * البحث عن كتب بالعنوان أو اسم المؤلف أو الاتنين مع بعض، وبترجع الكتب على صفحات (10 في كل صفحة).
     *
     * طريقة البحث:
     *  - النص بيتقسم لكلمات (بالمسافات).
     *  - كل كلمة لازم تكون موجودة في العنوان أو في اسم المؤلف (جزء من الكلمة كفاية).
     *  - مش مهم حروف كبيرة ولا صغيرة.
     *
     * أمثلة:
     *  - "clean"          → كل كتاب عنوانه أو مؤلفه فيه clean
     *  - "martin"         → كل كتاب مؤلفه فيه martin
     *  - "clean martin"   → الكتب اللي فيها clean وmartin (في العنوان أو المؤلف)
     *
     * لو النص فاضي، بترجع كل الكتب بالصفحات زي getAllBooks.
     *
     * @param text النص المطلوب البحث عنه
     * @param page رقم الصفحة (بنبدأ من 1)
     * @return قائمة الكتب المطابقة في الصفحة دي (10 على الأكثر، وفاضية لو مفيش نتايج)
     * @throws SQLException لو حصلت مشكلة في الاتصال أو القراءة
     */
    public List<Book> search(String text, int page) throws SQLException {

        // لو رقم الصفحة أقل من 1 نعتبرها الصفحة الأولى
        if (page < 1) {
            page = 1;
        }

        // نقسم نص البحث لكلمات
        String[] words = splitWords(text);

        // لو مفيش كلمات (النص فاضي) نرجع كل الكتب بالصفحات
        if (words.length == 0) {
            return getAllBooks(page);
        }

        // نفس حساب الصفحات اللي في getAllBooks: نتخطى كتب الصفحات اللي قبلها
        int offset = (page - 1) * PAGE_SIZE;

        // أمر SQL: نفس getAllBooks بس مع شرط WHERE بيفلتر الكتب المطابقة للكلمات
        String sql = "SELECT id, title, author, total_pages, pages_read "
                + "FROM books WHERE " + buildCondition(words)
                + " ORDER BY id LIMIT ? OFFSET ?";

        // القائمة اللي هنجمع فيها الكتب المطابقة
        List<Book> books = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            // نحط الكلمات مكان علامات الاستفهام، وبترجع رقم علامة الاستفهام اللي بعدها
            int next = bindWords(statement, words);

            // بعد الكلمات بييجي LIMIT وOFFSET
            statement.setInt(next, PAGE_SIZE);
            statement.setInt(next + 1, offset);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    // نحول كل صف لكتاب ونحط رقمه (ID)
                    Book book = new Book(
                            resultSet.getString("title"),
                            resultSet.getString("author"),
                            resultSet.getInt("total_pages"),
                            resultSet.getInt("pages_read")
                    );
                    book.setId(resultSet.getInt("id"));
                    books.add(book);
                }
            }
        }

        return books;
    }

    /**
     * نسخة مختصرة من search: بترجع الصفحة الأولى بس (أول 10 نتايج).
     */
    public List<Book> search(String text) throws SQLException {
        return search(text, 1);
    }

    /**
     * بترجع عدد صفحات نتايج البحث الكلي (10 كتب في الصفحة).
     * مفيدة عشان تعرض "صفحة 1 من 3" وتعرف إمتى توقف.
     * لو النص فاضي بترجع عدد صفحات كل الكتب.
     *
     * @param text نفس نص البحث اللي بتبعته لدالة search
     * @return عدد الصفحات (صفر لو مفيش نتايج)
     */
    public int getSearchTotalPages(String text) throws SQLException {

        String[] words = splitWords(text);

        // لو النص فاضي يبقى النتايج هي كل الكتب
        if (words.length == 0) {
            return getTotalPages();
        }

        // أمر SQL بيعد الكتب المطابقة بس
        String sql = "SELECT COUNT(*) FROM books WHERE " + buildCondition(words);

        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            bindWords(statement, words);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int totalMatches = resultSet.getInt(1);
                    // نقسم على 10 مع التقريب لأعلى
                    return (int) Math.ceil((double) totalMatches / PAGE_SIZE);
                }
            }
        }

        return 0;
    }

    // ==================== دوال مساعدة للبحث (خاصة بالكلاس ده) ====================

    /**
     * بتقسم نص البحث لكلمات بالمسافات.
     * لو النص null أو فاضي بترجع مصفوفة فاضية.
     */
    private String[] splitWords(String text) {
        if (text == null || text.isBlank()) {
            return new String[0];
        }
        // trim بتشيل المسافات من الأطراف، و\\s+ معناها "مسافة واحدة أو أكتر"
        return text.trim().split("\\s+");
    }

    /**
     * بتبني شرط SQL: لكل كلمة لازم تكون في العنوان أو في المؤلف، وكل الكلمات لازم تتحقق (AND).
     * ILIKE معناها "LIKE" بس من غير حساسية لحروف كبيرة أو صغيرة.
     * مثال لكلمتين: (title ILIKE ? OR author ILIKE ?) AND (title ILIKE ? OR author ILIKE ?)
     */
    private String buildCondition(String[] words) {
        StringBuilder condition = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            if (i > 0) {
                condition.append(" AND ");
            }
            condition.append("(title ILIKE ? OR author ILIKE ?)");
        }
        return condition.toString();
    }

    /**
     * بتحط كل كلمة مكان علامات الاستفهام اللي في الشرط (علامتين لكل كلمة: العنوان والمؤلف).
     * علامتا % حوالين الكلمة معناها "أي نص قبلها وبعدها"، فالجزء من الكلمة كفاية.
     *
     * @return رقم علامة الاستفهام اللي بعد آخر كلمة (عشان نكمل بعدها LIMIT وOFFSET)
     */
    private int bindWords(PreparedStatement statement, String[] words) throws SQLException {
        int index = 1;
        for (String word : words) {
            String pattern = "%" + escapeLike(word) + "%";
            statement.setString(index++, pattern); // للعنوان
            statement.setString(index++, pattern); // للمؤلف
        }
        return index;
    }

    /**
     * بتعطّل الرموز الخاصة في LIKE (% و _ و \) عشان لو المستخدم كتبها
     * تتعامل كحروف عادية مش كرموز بحث.
     * مثال: لو كتب "100%" هندوّر على النص "100%" بالظبط مش على أي حاجة بتبدأ بـ 100.
     */
    private String escapeLike(String word) {
        return word
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}