import java.util.ArrayList;
import java.util.List;

/**
 * الكلاس BookFormatter (منسّق عرض الكتاب)
 * ------------------------------------------------------------
 * مسؤوليته الوحيدة: تحويل كتاب (Book) لنص منسق شكله شبه رسومي
 * (إطار + شريط تقدم).
 *
 * فصلناه عن كلاس Book عشان Book يفضل مسؤول عن بيانات الكتاب بس،
 * وأي تغيير في شكل العرض (رموز الإطار، طول الشريط...) يتعمل هنا من غير ما نلمس Book.
 *
 * كل دالة هنا ليها شغلة واحدة بس:
 *   format             → بتنسّق الخطوات وبترجع النص النهائي
 *   buildProgressBar   → بتبني شريط التقدم
 *   buildLines         → بتجهز سطور البيانات اللي هتظهر جوه الإطار
 *   findLongestLine    → بتحسب أطول سطر
 *   buildFrame         → بترسم الإطار حوالين السطور
 */
public final class BookFormatter {

    // طول شريط التقدم: 20 خانة
    private static final int BAR_LENGTH = 20;

    // الرمز اللي بنرسم بيه الجزء المقروء من الشريط
    private static final char FILLED = '█';

    // الرمز اللي بنرسم بيه الجزء المتبقي من الشريط
    private static final char EMPTY = '░';

    // منع إنشاء كائنات من الكلاس ده: كل دوالنا static فمفيش داعي لكائن
    private BookFormatter() {
    }

    /**
     * الدالة الوحيدة المتاحة للاستخدام من بره: بتحول الكتاب لنص منسق.
     * بتنادي على الدوال التانية بالترتيب وبس، من غير ما تعمل شغل بنفسها.
     *
     * @param book الكتاب المطلوب عرضه
     * @return النص المنسق (إطار فيه بيانات الكتاب)
     */
    public static String format(Book book) {
        // 1) نجهز السطور اللي هتظهر
        List<String> lines = buildLines(book);

        // 2) نعرف أطول سطر عشان نظبط عرض الإطار
        int width = findLongestLine(lines);

        // 3) نرسم الإطار حوالين السطور
        return buildFrame(lines, width);
    }

    /**
     * بتبني شريط التقدم حسب النسبة المئوية.
     * مثال: لو النسبة 50% يبقى 10 خانات من 20 تتملي.
     *
     * @param percentage نسبة التقدم (من 0 إلى 100)
     * @return شريط التقدم كنص، مثلًا: █████░░░░░░░░░░░░░░░
     */
    private static String buildProgressBar(double percentage) {
        // نحسب كام خانة هتتملي
        int filled = (int) Math.round(percentage / 100 * BAR_LENGTH);

        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < BAR_LENGTH; i++) {
            // الخانات الأولى ممتلئة (اتقرأت) والباقي فاضي (لسه)
            bar.append(i < filled ? FILLED : EMPTY);
        }
        return bar.toString();
    }

    /**
     * بتجهز سطور بيانات الكتاب بالترتيب اللي هتظهر بيه.
     * سطر الـ ID بيتضاف بس لو الكتاب محفوظ في القاعدة (رقمه أكبر من صفر).
     *
     * @param book الكتاب
     * @return قائمة السطور (كل عنصر سطر واحد)
     */
    private static List<String> buildLines(Book book) {
        List<String> lines = new ArrayList<>();

        // الكتاب المحفوظ بس هو اللي ليه ID يتعرض
        if (book.getId() > 0) {
            lines.add("ID       : " + book.getId());
        }

        lines.add("Title    : " + book.getTitle());
        lines.add("Author   : " + book.getAuthor());
        lines.add("Pages    : " + book.getPagesRead() + " / " + book.getTotalPages());

        // سطر التقدم: الشريط + النسبة المئوية برقم عشري واحد
        double percentage = book.getProgressPercentage();
        lines.add("Progress : " + buildProgressBar(percentage)
                + " " + String.format("%.1f", percentage) + "%");

        return lines;
    }

    /**
     * بتحسب طول أطول سطر في القائمة.
     *
     * @param lines السطور
     * @return عدد حروف أطول سطر
     */
    private static int findLongestLine(List<String> lines) {
        int longest = 0;
        for (String line : lines) {
            longest = Math.max(longest, line.length());
        }
        return longest;
    }

    /**
     * بترسم الإطار حوالين السطور: حد علوي + السطور + حد سفلي.
     *
     * @param lines السطور اللي هتتحط جوه الإطار
     * @param width عرض الإطار (طول أطول سطر)
     * @return الإطار كامل كنص
     */
    private static String buildFrame(List<String> lines, int width) {
        // الخط الأفقي للإطار: الرمز "═" متكرر بعدد العرض (+2 للمسافات على الجانبين)
        String border = "═".repeat(width + 2);

        // %-Ns معناها: اطبع النص وكمّل بمسافات لحد N حرف، عشان الحافة اليمين تتساوى
        String rowFormat = "║ %-" + width + "s ║\n";

        StringBuilder frame = new StringBuilder();
        frame.append("╔").append(border).append("╗\n");
        for (String line : lines) {
            frame.append(String.format(rowFormat, line));
        }
        frame.append("╚").append(border).append("╝");

        return frame.toString();
    }
}