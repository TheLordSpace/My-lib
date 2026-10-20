import java.sql.SQLException;
import java.util.List;
import java.util.OptionalInt;

/**
 * الكلاس BookService (منطق العمل)
 * ------------------------------------------------------------
 * مسؤوليته: العمليات اللي بتتعمل على الكتب (إنشاء، حذف، تحديث، عرض، بحث).
 *
 * مهم: الكلاس ده مفيهوش أي طباعة ولا قراءة من المستخدم،
 * بياخد قيم جاهزة وبيرجّع نتايج، والكلاس اللي مسؤول عن الشاشة هو ConsoleView.
 * وكمان مبيعرفش حاجة عن قاعدة البيانات نفسها، بيطلب الشغل من BookRepository وLibraryRepository.
 */
public class BookService {

    // المسؤول عن الحفظ والحذف والتحديث في القاعدة
    private final BookRepository bookRepository;

    // المسؤول عن قراءة الكتب والبحث فيها على صفحات
    private final LibraryRepository libraryRepository;

    public BookService(BookRepository bookRepository, LibraryRepository libraryRepository) {
        this.bookRepository = bookRepository;
        this.libraryRepository = libraryRepository;
    }

    /**
     * بتجهز قاعدة البيانات (بتنشئ جدول الكتب لو مش موجود).
     * بنناديها مرة واحدة في بداية البرنامج.
     */
    public void initialize() throws SQLException {
        bookRepository.createTableIfNotExists();
    }

    /**
     * بتنشئ كتاب جديد وتحفظه في القاعدة.
     *
     * @return الكتاب بعد الحفظ، ومعاه رقمه (ID)
     */
    public Book createBook(String title, String author, int totalPages, int pagesRead) throws SQLException {
        Book book = new Book(title, author, totalPages, pagesRead);

        // نحفظ الكتاب ونحط عليه الرقم اللي القاعدة أعطته
        int id = bookRepository.saveBook(book);
        book.setId(id);

        return book;
    }

    /**
     * بتحذف كتاب عن طريق رقمه.
     *
     * @return true لو الكتاب كان موجود واتحذف، وfalse لو مفيش كتاب بالرقم ده
     */
    public boolean deleteBook(int id) throws SQLException {
        return bookRepository.deleteBook(id);
    }

    /**
     * بتحدّث عدد الصفحات المقروءة لكتاب.
     * لو الرقم أكبر من صفحات الكتاب، بيتسجل عدد الصفحات الكلي بدله.
     *
     * @return عدد الصفحات المقروءة اللي اتخزن فعلًا، أو قيمة فاضية (empty) لو مفيش كتاب بالرقم ده
     */
    public OptionalInt updatePagesRead(int id, int pagesRead) throws SQLException {
        // BookRepository بترجع -1 لو الكتاب مش موجود، هنا بنحولها لقيمة فاضية أوضح في المعنى
        int stored = bookRepository.updatePagesRead(id, pagesRead);
        return (stored == -1) ? OptionalInt.empty() : OptionalInt.of(stored);
    }

    /**
     * بترجع كتب صفحة واحدة (10 كتب على الأكثر).
     *
     * @param searchText نص البحث (عنوان أو مؤلف أو جزء منهم). لو فاضي بترجع كل الكتب.
     * @param page       رقم الصفحة (بنبدأ من 1)
     */
    public List<Book> getBooksPage(String searchText, int page) throws SQLException {
        return libraryRepository.search(searchText, page);
    }

    /**
     * بترجع عدد الصفحات الكلي للنتايج.
     *
     * @param searchText نفس نص البحث اللي بتبعته لـ getBooksPage (فاضي = كل الكتب)
     */
    public int getTotalPages(String searchText) throws SQLException {
        return libraryRepository.getSearchTotalPages(searchText);
    }
}