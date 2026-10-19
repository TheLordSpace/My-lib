import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * الكلاس BookRepository (مخزن الكتب)
 * ------------------------------------------------------------
 * مسؤول عن التعامل مع قاعدة بيانات PostgreSQL.
 * بياخد كتاب (Book) ويحفظه في جدول اسمه books.
 *
 * مهم: لازم تضيف مكتبة تعريف PostgreSQL (JDBC Driver) للمشروع.
 *  - لو بتستخدم Maven ضيف في pom.xml:
 *      <dependency>
 *          <groupId>org.postgresql</groupId>
 *          <artifactId>postgresql</artifactId>
 *          <version>42.7.4</version>
 *      </dependency>
 *  - لو بتشغل من غير Maven، نزّل ملف postgresql-xx.jar وحطه في الـ classpath.
 */
public class BookRepository {

    // عنوان الاتصال بقاعدة البيانات: الجهاز (localhost) والبورت (5432) واسم القاعدة (bookdb)
    private final String url;

    // اسم المستخدم بتاع قاعدة البيانات
    private final String user;

    // كلمة سر المستخدم
    private final String password;

    /**
     * دالة الإنشاء: بنبعتلها بيانات الاتصال بالقاعدة مرة واحدة،
     * وبعد كده تستخدمها في كل العمليات.
     *
     * مثال على الـ url: "jdbc:postgresql://localhost:5432/bookdb"
     */
    public BookRepository(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    /**
     * دالة بتنشئ جدول books لو مش موجود.
     * بنناديها مرة واحدة في بداية البرنامج عشان نضمن إن الجدول جاهز.
     * IF NOT EXISTS معناها: لو الجدول موجود قبل كده متعملش حاجة ومتطلعش خطأ.
     */
    public void createTableIfNotExists() throws SQLException {
        // أمر SQL بينشئ الجدول وأعمدته:
        // id          : رقم تلقائي لكل كتاب (بيزيد لوحده 1، 2، 3...)
        // title       : اسم الكتاب (نص، مطلوب)
        // author      : اسم المؤلف (نص، مطلوب)
        // total_pages : عدد الصفحات الكلي (رقم)
        // pages_read  : عدد الصفحات المقروءة (رقم، الافتراضي صفر)
        String sql = """
                CREATE TABLE IF NOT EXISTS books (
                    id          SERIAL PRIMARY KEY,
                    title       VARCHAR(255) NOT NULL,
                    author      VARCHAR(255) NOT NULL,
                    total_pages INTEGER      NOT NULL CHECK (total_pages >= 0),
                    pages_read  INTEGER      NOT NULL DEFAULT 0 CHECK (pages_read >= 0)
                )
                """;

        // try-with-resources: بيقفل الاتصال تلقائيًا لما نخلص، حتى لو حصل خطأ
        try (Connection connection = DriverManager.getConnection(url, user, password);
             Statement statement = connection.createStatement()) {
            // ننفذ أمر إنشاء الجدول
            statement.execute(sql);
        }
    }

    /**
     * الدالة الأساسية: بتستلم كتاب وتحفظه في قاعدة البيانات.
     *
     * @param book الكتاب المطلوب حفظه
     * @return الرقم (id) اللي قاعدة البيانات أعطته للكتاب بعد الحفظ
     * @throws SQLException لو حصلت مشكلة في الاتصال أو في الحفظ
     */
    public int saveBook(Book book) throws SQLException {

        // نتأكد إن الكتاب مش فاضي (null) قبل ما نحاول نحفظه
        if (book == null) {
            throw new IllegalArgumentException("الكتاب لا يمكن أن يكون فارغًا");
        }

        // أمر SQL لإضافة صف جديد في الجدول.
        // علامات الاستفهام (?) أماكن محجوزة للقيم، هنملاها تحت بشكل آمن.
        // RETURNING id بتخلي PostgreSQL يرجعلنا رقم الكتاب الجديد.
        String sql = "INSERT INTO books (title, author, total_pages, pages_read) "
                + "VALUES (?, ?, ?, ?) RETURNING id";

        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            // نحط بيانات الكتاب مكان علامات الاستفهام بالترتيب (من 1 لـ 4).
            // استخدام PreparedStatement بدل ما نلصق النصوص بإيدينا بيحمي من هجمات SQL Injection.
            statement.setString(1, book.getTitle());      // اسم الكتاب
            statement.setString(2, book.getAuthor());     // اسم المؤلف
            statement.setInt(3, book.getTotalPages());    // عدد الصفحات الكلي
            statement.setInt(4, book.getPagesRead());     // عدد الصفحات المقروءة

            // ننفذ الأمر ونستلم النتيجة (رقم الكتاب الجديد)
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    // نرجع الرقم اللي القاعدة أنشأته
                    return resultSet.getInt("id");
                }
            }
        }

        // لو وصلنا هنا يبقى القاعدة ما رجعتش رقم، وده مش متوقع
        throw new SQLException("فشل حفظ الكتاب: لم يتم إرجاع رقم الكتاب");
    }

    /**
     * دالة بتحذف كتاب من قاعدة البيانات عن طريق رقمه (id).
     *
     * @param id رقم الكتاب المطلوب حذفه (هو نفس الرقم اللي رجعته saveBook)
     * @return true لو الكتاب كان موجود واتحذف، وfalse لو مفيش كتاب بالرقم ده
     * @throws SQLException لو حصلت مشكلة في الاتصال أو في الحذف
     */
    public boolean deleteBook(int id) throws SQLException {

        // أمر SQL بيحذف الصف اللي رقمه (id) بيساوي القيمة اللي هنبعتها.
        // علامة الاستفهام (?) مكان محجوز للرقم، هنملاه تحت بشكل آمن.
        String sql = "DELETE FROM books WHERE id = ?";

        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            // نحط رقم الكتاب مكان علامة الاستفهام
            statement.setInt(1, id);

            // executeUpdate بتنفذ الحذف وبترجع عدد الصفوف اللي اتحذفت:
            //   1 → الكتاب كان موجود واتحذف
            //   0 → مفيش كتاب بالرقم ده، فمحصلش حذف
            int deletedRows = statement.executeUpdate();

            // نرجع true لو اتحذف صف فعلًا
            return deletedRows > 0;
        }
    }

    /**
     * دالة بتحدّث عدد الصفحات المقروءة لكتاب معين عن طريق رقمه (id).
     * لو الرقم الجديد أكبر من عدد صفحات الكتاب الكلي، بيتحط عدد الصفحات الكلي
     * (لأن مينفعش تقرأ صفحات أكتر من اللي في الكتاب)، زي ما بتعمل دالة إنشاء Book.
     *
     * @param id        رقم الكتاب المطلوب تحديثه
     * @param pagesRead عدد الصفحات المقروءة الجديد (لازم يكون صفر أو أكبر)
     * @return عدد الصفحات المقروءة اللي اتخزن فعلًا في القاعدة،
     *         أو -1 لو مفيش كتاب بالرقم ده
     * @throws SQLException لو حصلت مشكلة في الاتصال أو في التحديث
     */
    public int updatePagesRead(int id, int pagesRead) throws SQLException {

        // نتأكد إن الرقم مش سالب قبل ما نكلم القاعدة
        if (pagesRead < 0) {
            throw new IllegalArgumentException("عدد الصفحات المقروءة لا يمكن أن يكون سالبًا");
        }

        // أمر SQL بيحدّث عمود pages_read للكتاب اللي رقمه (id).
        // LEAST(?, total_pages) بتختار الأصغر بين الرقم الجديد وعدد الصفحات الكلي،
        // فمستحيل الصفحات المقروءة تعدّي صفحات الكتاب.
        // RETURNING pages_read بترجعلنا القيمة اللي اتخزنت فعلًا بعد التحديث.
        String sql = "UPDATE books SET pages_read = LEAST(?, total_pages) "
                + "WHERE id = ? RETURNING pages_read";

        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            // نحط القيم مكان علامات الاستفهام بالترتيب
            statement.setInt(1, pagesRead); // عدد الصفحات المقروءة الجديد
            statement.setInt(2, id);        // رقم الكتاب

            // ننفذ الأمر ونستلم الصف المحدَّث (لو لقى الكتاب)
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    // الكتاب موجود واتحدّث: نرجع القيمة اللي اتخزنت
                    return resultSet.getInt("pages_read");
                }
            }
        }

        // مفيش صف رجع يعني مفيش كتاب بالرقم ده
        return -1;
    }
}