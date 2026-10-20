import java.util.List;
import java.util.Scanner;

/**
 * الكلاس ConsoleView (التعامل مع المستخدم)
 * ------------------------------------------------------------
 * مسؤوليته الوحيدة: كل اللي بيظهر على الشاشة وكل اللي بيتقرأ من الكيبورد
 * (القوائم، الرسائل، الأسئلة، التأكيدات).
 *
 * مفيهوش أي قرار في البرنامج ولا أي تعامل مع القاعدة،
 * هو بس بيعرض ويسأل ويرجّع اللي المستخدم كتبه.
 */
public class ConsoleView {

    /**
     * الأوامر المتاحة وإحنا بنتنقل بين صفحات النتايج.
     * (enum معناه مجموعة قيم ثابتة محدودة)
     */
    public enum PageCommand {
        NEXT,      // الصفحة التالية
        PREVIOUS,  // الصفحة السابقة
        BACK       // الرجوع للقائمة الرئيسية
    }

    // Scanner هو الأداة اللي بنقرأ بيها اللي المستخدم بيكتبه على الكيبورد
    private final Scanner scanner = new Scanner(System.in);

    // ==================== العرض ====================

    /** بتعرض القائمة الرئيسية. */
    public void showMenu() {
        System.out.println("\n===== برنامج الكتب =====");
        System.out.println("اكتب c لإنشاء كتاب");
        System.out.println("اكتب l لعرض الكتب");
        System.out.println("اكتب s للبحث عن كتاب");
        System.out.println("اكتب u لتحديث الصفحات المقروءة");
        System.out.println("اكتب d لحذف كتاب");
        System.out.println("اكتب q للخروج");
    }

    /** بتعرض رسالة عادية للمستخدم. */
    public void showMessage(String message) {
        System.out.println(message);
    }

    /** بتعرض كتاب واحد (بينادي toString بتاعته تلقائيًا). */
    public void showBook(Book book) {
        System.out.println();
        System.out.println(book);
    }

    /**
     * بتعرض صفحة من الكتب مع رقم الصفحة.
     *
     * @param title      عنوان القائمة (مثلًا: الكتب أو نتايج البحث)
     * @param page       رقم الصفحة الحالية
     * @param totalPages عدد الصفحات الكلي
     * @param books      كتب الصفحة دي
     */
    public void showPage(String title, int page, int totalPages, List<Book> books) {
        System.out.println("\n----- " + title + ": صفحة " + page + " من " + totalPages + " -----");
        for (Book book : books) {
            System.out.println(book);
            System.out.println();
        }
    }

    /** بتعرض رسالة خطأ مفهومة، وبتطبع تفاصيل الخطأ التقنية تحتها عشان المبرمج. */
    public void showError(Exception e) {
        System.out.println("حصل خطأ: " + e.getMessage());
        e.printStackTrace();
    }

    // ==================== القراءة ====================

    /** بتسأل المستخدم وبترجّع اللي كتبه (من غير مسافات زيادة). */
    public String readText(String message) {
        System.out.print(message);
        return scanner.nextLine().trim();
    }

    /**
     * بتسأل المستخدم وبترجّع اللي كتبه بحروف صغيرة،
     * عشان C و c يتعاملوا زي بعض.
     */
    public String readCommand(String message) {
        return readText(message).toLowerCase();
    }

    /**
     * بتفضل تسأل المستخدم لحد ما يكتب رقم صحيح موجب.
     * بتمنع البرنامج إنه يقع لو المستخدم كتب حروف بدل أرقام.
     *
     * @param message  الرسالة اللي هتظهر للمستخدم
     * @param optional لو true يبقى مسموح المستخدم يسيبها فاضية (وترجع صفر)
     * @return الرقم اللي كتبه المستخدم
     */
    public int readInt(String message, boolean optional) {
        while (true) {
            String input = readText(message);

            // لو المدخل فاضي والسؤال اختياري، نرجع صفر
            if (input.isEmpty() && optional) {
                return 0;
            }

            try {
                // نحاول نحول النص لرقم صحيح
                int number = Integer.parseInt(input);

                // نتأكد إن الرقم مش سالب
                if (number >= 0) {
                    return number;
                }
                System.out.println("لازم الرقم يكون صفر أو أكبر.");

            } catch (NumberFormatException e) {
                // لو التحويل فشل (المستخدم كتب حروف مثلًا) نبلغه ونسأله تاني
                System.out.println("من فضلك اكتب رقم صحيح.");
            }
        }
    }

    /**
     * بتسأل المستخدم سؤال تأكيد.
     *
     * @return true لو كتب y، وأي حاجة تانية معناها إلغاء (false)
     */
    public boolean confirm(String question) {
        return readCommand(question + " (y = نعم / أي حرف آخر = إلغاء): ").equals("y");
    }

    /**
     * بتسأل المستخدم عايز يعمل إيه وهو بيتنقل بين الصفحات،
     * وبتفضل تسأله لحد ما يكتب أمر صحيح (n أو p أو b).
     */
    public PageCommand readPageCommand() {
        while (true) {
            String command = readCommand("n = التالية | p = السابقة | b = رجوع : ");

            if (command.equals("n")) {
                return PageCommand.NEXT;
            } else if (command.equals("p")) {
                return PageCommand.PREVIOUS;
            } else if (command.equals("b")) {
                return PageCommand.BACK;
            }

            System.out.println("اختيار غير صحيح، اكتب n أو p أو b فقط.");
        }
    }

    /** بتقفل الـ Scanner لما نخلص. */
    public void close() {
        scanner.close();
    }
}