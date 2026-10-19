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
}