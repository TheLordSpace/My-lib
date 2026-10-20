import java.sql.SQLException;
import java.util.List;
import java.util.OptionalInt;

/**
 * الكلاس BookController (التحكم في تدفق البرنامج)
 * ------------------------------------------------------------
 * مسؤوليته: ترتيب الخطوات.
 *   - الحلقة الرئيسية (قائمة → اختيار → تنفيذ → رجوع للقائمة).
 *   - حلقة التنقل بين الصفحات.
 *   - تحديد مين يتنادى إمتى: يسأل المستخدم عن طريق ConsoleView،
 *     وينفذ العملية عن طريق BookService، ويعرض النتيجة عن طريق ConsoleView.
 *
 * هو نفسه مبيطبعش ولا بيقرأ من الكيبورد ولا بيتعامل مع القاعدة مباشرة.
 */
public class BookController {

    // المسؤول عن منطق العمل (إنشاء، حذف، تحديث، بحث)
    private final BookService service;

    // المسؤول عن الشاشة والكيبورد
    private final ConsoleView view;

    public BookController(BookService service, ConsoleView view) {
        this.service = service;
        this.view = view;
    }

    // ==================== الحلقة الرئيسية ====================

    /**
     * بتشغّل البرنامج: بتجهز القاعدة، وبعدين بتفضل تعرض القائمة
     * وتنفذ اللي المستخدم يختاره لحد ما يكتب q.
     */
    public void run() {

        // نجهز القاعدة (ننشئ الجدول لو مش موجود)
        try {
            service.initialize();
        } catch (SQLException e) {
            view.showError(e);
        }

        // متغير بيفضل البرنامج شغال طول ما قيمته true، ولما المستخدم يكتب q بنخليه false
        boolean running = true;

        while (running) {
            view.showMenu();
            String choice = view.readCommand("اختيارك: ");

            // أي خطأ في القاعدة بيتمسك هنا في مكان واحد، فنعرضه ونرجع للقائمة
            try {
                switch (choice) {
                    case "c" -> createBook();
                    case "l" -> browse("الكتب", "لا توجد كتب محفوظة حتى الآن.", "");
                    case "s" -> searchBooks();
                    case "u" -> updatePagesRead();
                    case "d" -> deleteBook();
                    case "q" -> {
                        view.showMessage("مع السلامة!");
                        running = false;
                    }
                    default -> view.showMessage("اختيار غير صحيح، اكتب c أو l أو s أو u أو d أو q فقط.");
                }
            } catch (SQLException e) {
                view.showError(e);
            }
        }

        view.close();
    }

    // ==================== خطوات كل اختيار ====================

    /** إنشاء كتاب: نسأل عن البيانات، ننشئ الكتاب ونحفظه، ونعرضه. */
    private void createBook() throws SQLException {
        String title = view.readText("اسم الكتاب: ");
        String author = view.readText("اسم المؤلف: ");
        int totalPages = view.readInt("عدد الصفحات: ", false);

        // الصفحات المقروءة اختيارية: لو المستخدم داس Enter بتبقى صفر
        int pagesRead = view.readInt("عدد الصفحات المقروءة (اضغط Enter لو صفر): ", true);

        Book book = service.createBook(title, author, totalPages, pagesRead);
        view.showBook(book);
    }

    /** حذف كتاب: نسأل عن الرقم، نطلب تأكيد، وننفذ الحذف. */
    private void deleteBook() throws SQLException {
        int id = view.readInt("رقم الكتاب (ID) المراد حذفه: ", false);

        // الحذف مش بيترجع فيه، فنطلب من المستخدم يأكد الأول
        if (!view.confirm("هل أنت متأكد من حذف الكتاب رقم " + id + "؟")) {
            view.showMessage("تم إلغاء الحذف.");
            return;
        }

        if (service.deleteBook(id)) {
            view.showMessage("تم حذف الكتاب رقم " + id + " بنجاح.");
        } else {
            view.showMessage("لا يوجد كتاب بالرقم " + id + ".");
        }
    }

    /** تحديث الصفحات المقروءة: نسأل عن الرقم والقيمة الجديدة وننفذ التحديث. */
    private void updatePagesRead() throws SQLException {
        int id = view.readInt("رقم الكتاب (ID): ", false);

        // الإجمالي الجديد، مش اللي اتقرأ النهاردة بس
        int pagesRead = view.readInt("عدد الصفحات المقروءة الجديد: ", false);

        OptionalInt stored = service.updatePagesRead(id, pagesRead);

        if (stored.isEmpty()) {
            view.showMessage("لا يوجد كتاب بالرقم " + id + ".");
        } else if (stored.getAsInt() < pagesRead) {
            // الرقم اللي كتبه المستخدم أكبر من صفحات الكتاب، فاتحط عدد الصفحات الكلي
            view.showMessage("الرقم أكبر من عدد صفحات الكتاب، فتم تسجيل " + stored.getAsInt() + " (كل الصفحات).");
        } else {
            view.showMessage("تم التحديث: الصفحات المقروءة الآن " + stored.getAsInt() + ".");
        }
    }

    /** البحث: نسأل عن النص، وبعدين نعرض النتايج بالصفحات. */
    private void searchBooks() throws SQLException {
        String text = view.readText("اكتب العنوان أو اسم المؤلف (أو جزء منهم): ");

        // لو المستخدم مكتبش حاجة نرجع للقائمة (عرض كل الكتب ده دور الاختيار l)
        if (text.isEmpty()) {
            view.showMessage("لم تكتب نص للبحث.");
            return;
        }

        browse("نتايج البحث", "لا توجد كتب مطابقة للبحث.", text);
    }

    // ==================== حلقة التنقل بين الصفحات ====================

    /**
     * بتعرض الكتب صفحة صفحة وبتخلي المستخدم يتنقل بينها.
     * بنستخدمها في عرض كل الكتب وفي نتايج البحث (كان فيه حلقتين متطابقتين، بقت واحدة).
     *
     * @param title        عنوان القائمة اللي بيظهر فوق كل صفحة
     * @param emptyMessage الرسالة اللي بتظهر لو مفيش ولا كتاب
     * @param searchText   نص البحث، وفاضي معناه كل الكتب
     */
    private void browse(String title, String emptyMessage, String searchText) throws SQLException {

        // بنبدأ من الصفحة الأولى
        int currentPage = 1;

        while (true) {
            int totalPages = service.getTotalPages(searchText);

            // لو مفيش نتايج نبلغ المستخدم ونرجع للقائمة
            if (totalPages == 0) {
                view.showMessage(emptyMessage);
                return;
            }

            // نتأكد إن رقم الصفحة جوه الحدود (من 1 لحد آخر صفحة)
            currentPage = Math.min(Math.max(currentPage, 1), totalPages);

            // نجيب كتب الصفحة الحالية ونعرضها
            List<Book> books = service.getBooksPage(searchText, currentPage);
            view.showPage(title, currentPage, totalPages, books);

            // نسأل المستخدم عايز يروح فين
            switch (view.readPageCommand()) {
                case NEXT -> currentPage++;
                case PREVIOUS -> currentPage--;
                case BACK -> {
                    return;
                }
            }
        }
    }
}