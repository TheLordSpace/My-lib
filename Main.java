/**
 * الكلاس Main (نقطة البداية)
 * ------------------------------------------------------------
 * مسؤوليته الوحيدة: يجمّع أجزاء البرنامج مع بعض ويشغّلها.
 * مفيهوش منطق ولا طباعة ولا حلقات، كل ده في الكلاسات التانية:
 *
 *   ConsoleView     → الشاشة والكيبورد (التعامل مع المستخدم)
 *   BookService     → منطق العمل (إنشاء، حذف، تحديث، بحث)
 *   BookController  → ترتيب الخطوات (الحلقة الرئيسية والتنقل بين الصفحات)
 *   BookRepository / LibraryRepository → الكلام مع قاعدة البيانات
 *
 * طريقة التشغيل:
 *   java Main <رابط القاعدة> <اسم المستخدم> <كلمة السر>
 *   مثال: java Main jdbc:postgresql://localhost:5432/bookdb postgres your_password
 */
public class Main {

    public static void main(String[] args) {

        // نتأكد إن المستخدم بعت بيانات الاتصال التلاتة
        if (args.length < 3) {
            System.out.println("الاستخدام: java Main <url> <username> <password>");
            return;
        }

        String url = args[0];
        String username = args[1];
        String password = args[2];

        // طبقة قاعدة البيانات
        BookRepository bookRepository = new BookRepository(url, username, password);
        LibraryRepository libraryRepository = new LibraryRepository(url, username, password);

        // طبقة منطق العمل (بتستخدم طبقة القاعدة)
        BookService service = new BookService(bookRepository, libraryRepository);

        // طبقة الشاشة
        ConsoleView view = new ConsoleView();

        // المتحكم بيربط الشاشة بمنطق العمل، وبنشغّله
        new BookController(service, view).run();
    }
}