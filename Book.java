/**
 * الكلاس Book (كتاب)
 * ------------------------------------------------------------
 * الكلاس ده بيمثل كتاب في الحياة الحقيقية.
 * زي ما الكتاب ليه اسم ومؤلف وعدد صفحات، الكلاس برضه بيخزن
 * نفس المعلومات دي، وبيحسب لك نسبة التقدم في القراءة.
 */
public class Book {

    // ==================== الخصائص (البيانات) ====================

    // اسم الكتاب (نص)
    private String title;

    // اسم المؤلف (نص)
    private String author;

    // العدد الكلي لصفحات الكتاب (رقم صحيح)
    private int totalPages;

    // عدد الصفحات اللي اتقرأت لحد دلوقتي (رقم صحيح)
    private int pagesRead;

    // رقم الكتاب (ID) في قاعدة البيانات.
    // بيكون صفر لو الكتاب لسه متحفظش، وبيتحدد بعد الحفظ أو عند القراءة من القاعدة.
    // بنستخدمه عشان نحدد أنهي كتاب نحذفه.
    private int id;

    // ==================== دوال الإنشاء (Constructors) ====================

    /**
     * دالة إنشاء كاملة: بتاخد كل البيانات بما فيها الصفحات المقروءة.
     * بنستخدمها لما نعرف الكتاب وعدد الصفحات اللي اتقرأت منه.
     *
     * @param title      اسم الكتاب
     * @param author     اسم المؤلف
     * @param totalPages العدد الكلي للصفحات
     * @param pagesRead  عدد الصفحات المقروءة
     */
    public Book(String title, String author, int totalPages, int pagesRead) {
        // this.title يعني "خاصية الكتاب نفسه"، وtitle على اليمين هي القيمة اللي اتبعتت
        this.title = title;
        this.author = author;

        // نتأكد إن عدد الصفحات الكلي مش سالب (مش منطقي يكون كتاب بصفحات سالبة)
        this.totalPages = Math.max(totalPages, 0);

        // نتأكد إن الصفحات المقروءة مش سالبة، وكمان مش أكبر من العدد الكلي
        // (مينفعش تقرأ 300 صفحة من كتاب 200 صفحة!)
        this.pagesRead = Math.min(Math.max(pagesRead, 0), this.totalPages);
    }

    /**
     * دالة إنشاء مختصرة: من غير الصفحات المقروءة.
     * لو مبعتناش الصفحات المقروءة، بتتحط صفر تلقائيًا
     * (يعني لسه مبدأناش نقرأ الكتاب).
     *
     * @param title      اسم الكتاب
     * @param author     اسم المؤلف
     * @param totalPages العدد الكلي للصفحات
     */
    public Book(String title, String author, int totalPages) {
        // بننادي على الدالة الكاملة اللي فوق ونبعت صفر كقيمة للصفحات المقروءة
        this(title, author, totalPages, 0);
    }

    // ==================== دوال الحصول على القيم (Getters) ====================

    // بترجع اسم الكتاب
    public String getTitle() {
        return title;
    }

    // بترجع اسم المؤلف
    public String getAuthor() {
        return author;
    }

    // بترجع العدد الكلي للصفحات
    public int getTotalPages() {
        return totalPages;
    }

    // بترجع عدد الصفحات المقروءة
    public int getPagesRead() {
        return pagesRead;
    }

    // بترجع رقم الكتاب (ID) في قاعدة البيانات (صفر لو لسه متحفظش)
    public int getId() {
        return id;
    }

    // بتحدد رقم الكتاب (ID)، بنناديها بعد ما نحفظ الكتاب أو نقرأه من القاعدة
    public void setId(int id) {
        this.id = id;
    }

    // ==================== دالة نسبة التقدم ====================

    /**
     * بتحسب نسبة التقدم في القراءة كنسبة مئوية (من 0 إلى 100).
     * المعادلة: (الصفحات المقروءة ÷ العدد الكلي للصفحات) × 100
     * مثال: قرأت 50 من 200 صفحة → النسبة 25%
     *
     * @return نسبة التقدم كرقم عشري
     */
    public double getProgressPercentage() {
        // لو الكتاب عدد صفحاته صفر، مينفعش نقسم على صفر (هيحصل خطأ)
        // فبنرجع صفر على طول
        if (totalPages == 0) {
            return 0.0;
        }

        // (double) بتحول الرقم لعشري عشان القسمة تطلع بالكسور مش رقم صحيح بس
        return ((double) pagesRead / totalPages) * 100;
    }

    // ==================== دالة toString (العرض الشكلي) ====================

    /**
     * بتحول بيانات الكتاب لنص منسق شكله شبه الرسومي (إطار + شريط تقدم).
     * الدالة دي بتشتغل تلقائيًا لما تعمل System.out.println(book).
     */
    @Override
    public String toString() {
        // طول شريط التقدم: 20 خانة
        int barLength = 20;

        // نحسب كام خانة من الشريط هتتملي حسب النسبة
        // مثال: لو النسبة 50% يبقى 10 خانات من 20 تتملي
        int filled = (int) Math.round(getProgressPercentage() / 100 * barLength);

        // نبني شريط التقدم: "█" للجزء المقروء و"░" للجزء المتبقي
        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < barLength; i++) {
            if (i < filled) {
                bar.append("█"); // خانة ممتلئة (اتقرأت)
            } else {
                bar.append("░"); // خانة فاضية (لسه)
            }
        }

        // نجهز سطور الإطار (كل سطر نص عادي)
        String line1 = "Title    : " + title;
        String line2 = "Author   : " + author;
        String line3 = "Pages    : " + pagesRead + " / " + totalPages;
        String line4 = "Progress : " + bar + " " + String.format("%.1f", getProgressPercentage()) + "%";

        // سطر رقم الكتاب (ID): بيظهر بس لو الكتاب محفوظ في القاعدة (رقمه أكبر من صفر)
        String idLine = "ID       : " + id;

        // نحدد أطول سطر عشان نظبط عرض الإطار على حسبه
        int width = Math.max(
                Math.max(line1.length(), line2.length()),
                Math.max(line3.length(), line4.length())
        );

        // لو سطر الـ ID هيظهر، نتأكد إن الإطار واسع كفاية له
        if (id > 0) {
            width = Math.max(width, idLine.length());
        }

        // نعمل الخط الأفقي للإطار بتكرار الرمز "═" بعدد العرض
        String border = "═".repeat(width + 2);

        // نجمع كل حاجة في نص واحد: الحد العلوي + السطور + الحد السفلي
        // %-Ns معناها: اطبع النص وكمّل بمسافات لحد N حرف (عشان الحافة اليمين تتساوى)
        String format = "║ %-" + width + "s ║\n";
        StringBuilder sb = new StringBuilder();
        sb.append("╔").append(border).append("╗\n");
        // نضيف سطر الـ ID أول سطر في الإطار (لو الكتاب محفوظ في القاعدة)
        if (id > 0) {
            sb.append(String.format(format, idLine));
        }
        sb.append(String.format(format, line1));
        sb.append(String.format(format, line2));
        sb.append(String.format(format, line3));
        sb.append(String.format(format, line4));
        sb.append("╚").append(border).append("╝");

        return sb.toString();
    }

    // ==================== دالة main للتجربة ====================

    /**
     * نقطة بداية البرنامج: بنجرب فيها الكلاس بكتابين.
     */
    public static void main(String[] args) {
        // كتاب أول: بعتنا الصفحات المقروءة (75 من 300)
        Book book1 = new Book("Clean Code", "Robert C. Martin", 300, 75);

        // كتاب تاني: من غير الصفحات المقروءة، فهتبقى صفر تلقائيًا
        Book book2 = new Book("Effective Java", "Joshua Bloch", 416);

        // طباعة الكتابين (بتنادي toString تلقائيًا)
        System.out.println(book1);
        System.out.println();
        System.out.println(book2);
    }
}