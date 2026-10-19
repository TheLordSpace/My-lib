import java.util.List;
import java.util.Scanner;

/**
 * الكلاس Main (البرنامج الرئيسي)
 * ------------------------------------------------------------
 * ده الكلاس اللي بيتشغل أول ما نفتح البرنامج.
 * بيعرض للمستخدم الاختيارات دي:
 *   c  → إنشاء كتاب جديد وحفظه في قاعدة البيانات
 *   l  → عرض الكتب المحفوظة (10 كتب في كل صفحة)
 *   d  → حذف كتاب عن طريق رقمه (ID)
 *   s  → البحث عن كتب بالعنوان أو اسم المؤلف (10 نتايج في كل صفحة)
 *   u  → تحديث عدد الصفحات المقروءة لكتاب عن طريق رقمه (ID)
 *   q  → الخروج من البرنامج
 *
 * ملحوظة: لازم ملفات Book.java وBookRepository.java وLibraryRepository.java
 * تكون في نفس الفولدر مع الملف ده.
 */
public class Main {

    public static void main(String[] args) {

        String url = args[0];
        String username = args[1];
        String passwd = args[2];
        BookRepository bookRepository = new BookRepository(url, username, passwd);

        // كائن المكتبة: المسؤول عن قراءة الكتب من القاعدة على صفحات
        LibraryRepository libraryRepository = new LibraryRepository(url, username, passwd);

        try {
            bookRepository.createTableIfNotExists();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Scanner هو الأداة اللي بنقرأ بيها اللي المستخدم بيكتبه على الكيبورد
        Scanner scanner = new Scanner(System.in);

        // متغير بيفضل البرنامج شغال طول ما قيمته true
        // لما المستخدم يكتب q بنخليه false فالبرنامج يقف
        boolean running = true;

        // حلقة تكرار: بتفضل تعرض القائمة وتسأل المستخدم لحد ما يختار الخروج
        while (running) {

            // نعرض للمستخدم الاختيارات المتاحة
            System.out.println("\n===== برنامج الكتب =====");
            System.out.println("اكتب c لإنشاء كتاب");
            System.out.println("اكتب l لعرض الكتب");
            System.out.println("اكتب s للبحث عن كتاب");
            System.out.println("اكتب u لتحديث الصفحات المقروءة");
            System.out.println("اكتب d لحذف كتاب");
            System.out.println("اكتب q للخروج");
            System.out.print("اختيارك: ");

            // نقرأ اللي كتبه المستخدم، trim() بتشيل أي مسافات زيادة،
            // وtoLowerCase() بتحوله لحروف صغيرة عشان C و c يتعاملوا زي بعض
            String choice = scanner.nextLine().trim().toLowerCase();

            if (choice.equals("c")) {
                // المستخدم اختار إنشاء كتاب: ننادي على الدالة المسؤولة عن كده
                createBook(scanner, bookRepository);

            } else if (choice.equals("l")) {
                // المستخدم اختار عرض الكتب: ننادي على دالة العرض
                showBooks(scanner, libraryRepository);

            } else if (choice.equals("s")) {
                // المستخدم اختار البحث: ننادي على دالة البحث
                searchBooks(scanner, libraryRepository);

            } else if (choice.equals("u")) {
                // المستخدم اختار تحديث الصفحات المقروءة: ننادي على دالة التحديث
                updatePagesRead(scanner, bookRepository);

            } else if (choice.equals("d")) {
                // المستخدم اختار حذف كتاب: ننادي على دالة الحذف
                deleteBook(scanner, bookRepository);

            } else if (choice.equals("q")) {
                // المستخدم اختار الخروج: نوقف الحلقة ونودعه
                System.out.println("مع السلامة!");
                running = false;

            } else {
                // المستخدم كتب حاجة غير c أو l أو q: ننبهه ونرجع نعرض القائمة تاني
                System.out.println("اختيار غير صحيح، اكتب c أو l أو s أو u أو d أو q فقط.");
            }
        }

        // نقفل الـ Scanner لما نخلص (عادة حسنة في البرنامج)
        scanner.close();
    }

    /**
     * دالة بتسأل المستخدم عن بيانات الكتاب، وتنشئ كائن Book، وتطبعه.
     *
     * @param scanner الأداة اللي بنقرأ بيها من الكيبورد
     */
    private static void createBook(Scanner scanner,BookRepository bookRepository) {

        // نسأل عن اسم الكتاب ونخزنه
        System.out.print("اسم الكتاب: ");
        String title = scanner.nextLine().trim();

        // نسأل عن اسم المؤلف ونخزنه
        System.out.print("اسم المؤلف: ");
        String author = scanner.nextLine().trim();

        // نسأل عن عدد الصفحات الكلي (readInt بتتأكد إنه رقم صحيح)
        int totalPages = readInt(scanner, "عدد الصفحات: ", false);

        // نسأل عن الصفحات المقروءة، وده اختياري:
        // لو المستخدم داس Enter من غير ما يكتب حاجة، بنعتبره صفر
        int pagesRead = readInt(scanner, "عدد الصفحات المقروءة (اضغط Enter لو صفر): ", true);

        // ننشئ الكتاب بالبيانات اللي جمعناها
        // لو الصفحات المقروءة صفر نستخدم دالة الإنشاء المختصرة، وغير كده نستخدم الكاملة
        Book book;
        if (pagesRead == 0) {
            book = new Book(title, author, totalPages);
        } else {
            book = new Book(title, author, totalPages, pagesRead);
        }

        try {
            // نحفظ الكتاب في القاعدة ونحتفظ بالرقم (ID) اللي رجع،
            // عشان يظهر للمستخدم ويقدر يستخدمه لو عايز يحذف الكتاب بعد كده
            int id = bookRepository.saveBook(book);
            book.setId(id);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // نطبع الكتاب (بينادي toString اللي عملناها في Book تلقائيًا)
        System.out.println();
        System.out.println(book);
    }

    /**
     * دالة بتعرض الكتب المحفوظة في القاعدة، 10 كتب في كل صفحة،
     * وبتخلي المستخدم يتنقل بين الصفحات.
     *
     * الأوامر جوه العرض:
     *   n → الصفحة التالية
     *   p → الصفحة السابقة
     *   b → الرجوع للقائمة الرئيسية
     *
     * @param scanner           الأداة اللي بنقرأ بيها من الكيبورد
     * @param libraryRepository الكائن اللي بنجيب منه الكتب من القاعدة
     */
    private static void showBooks(Scanner scanner, LibraryRepository libraryRepository) {

        // رقم الصفحة الحالية، بنبدأ من الصفحة الأولى
        int currentPage = 1;

        // نفضل نعرض الصفحات لحد ما المستخدم يختار الرجوع
        while (true) {
            try {
                // نسأل القاعدة عن عدد الصفحات الكلي
                int totalPages = libraryRepository.getTotalPages();

                // لو مفيش ولا كتاب في القاعدة نبلغ المستخدم ونرجع للقائمة
                if (totalPages == 0) {
                    System.out.println("لا توجد كتب محفوظة حتى الآن.");
                    return;
                }

                // نتأكد إن رقم الصفحة الحالية جوه الحدود الصحيحة (من 1 لحد آخر صفحة)
                if (currentPage > totalPages) {
                    currentPage = totalPages;
                }
                if (currentPage < 1) {
                    currentPage = 1;
                }

                // نجيب كتب الصفحة الحالية (10 كتب على الأكثر) من الكائن LibraryRepository
                List<Book> books = libraryRepository.getAllBooks(currentPage);

                // نعرض رقم الصفحة وكل كتب الصفحة
                System.out.println("\n----- صفحة " + currentPage + " من " + totalPages + " -----");
                for (Book book : books) {
                    System.out.println(book);
                    System.out.println();
                }

            } catch (Exception e) {
                // لو حصلت مشكلة في الاتصال بالقاعدة نطبعها ونرجع للقائمة
                e.printStackTrace();
                return;
            }

            // نسأل المستخدم عايز يعمل إيه بعد كده
            System.out.print("n = التالية | p = السابقة | b = رجوع : ");
            String command = scanner.nextLine().trim().toLowerCase();

            if (command.equals("n")) {
                // الصفحة التالية (لو كنا في آخر صفحة، الكود فوق هيرجعنا لآخر صفحة)
                currentPage++;

            } else if (command.equals("p")) {
                // الصفحة السابقة (لو كنا في الأولى، الكود فوق هيخلينا فيها)
                currentPage--;

            } else if (command.equals("b")) {
                // الرجوع للقائمة الرئيسية
                return;

            } else {
                System.out.println("اختيار غير صحيح، اكتب n أو p أو b فقط.");
            }
        }
    }

    /**
     * دالة بتحذف كتاب من قاعدة البيانات عن طريق رقمه (ID).
     * رقم كل كتاب بيظهر فوق الكتاب لما تعرض الكتب بالاختيار l.
     *
     * @param scanner        الأداة اللي بنقرأ بيها من الكيبورد
     * @param bookRepository الكائن اللي بننادي منه دالة deleteBook
     */
    private static void deleteBook(Scanner scanner, BookRepository bookRepository) {

        // نسأل المستخدم عن رقم الكتاب المراد حذفه
        int id = readInt(scanner, "رقم الكتاب (ID) المراد حذفه: ", false);

        // الحذف مش بيترجع فيه، فنطلب من المستخدم يأكد الأول
        System.out.print("هل أنت متأكد من حذف الكتاب رقم " + id + "؟ (y = نعم / أي حرف آخر = إلغاء): ");
        String confirm = scanner.nextLine().trim().toLowerCase();

        // لو المستخدم مكتبش y نلغي العملية
        if (!confirm.equals("y")) {
            System.out.println("تم إلغاء الحذف.");
            return;
        }

        try {
            // ننادي deleteBook: بترجع true لو الكتاب كان موجود واتحذف، وfalse لو مش موجود
            boolean deleted = bookRepository.deleteBook(id);

            if (deleted) {
                System.out.println("تم حذف الكتاب رقم " + id + " بنجاح.");
            } else {
                System.out.println("لا يوجد كتاب بالرقم " + id + ".");
            }
        } catch (Exception e) {
            // لو حصلت مشكلة في الاتصال بالقاعدة نطبعها
            e.printStackTrace();
        }
    }

    /**
     * دالة بتحدّث عدد الصفحات المقروءة لكتاب عن طريق رقمه (ID).
     * رقم كل كتاب بيظهر فوق الكتاب لما تعرض الكتب بالاختيار l.
     *
     * @param scanner        الأداة اللي بنقرأ بيها من الكيبورد
     * @param bookRepository الكائن اللي بننادي منه دالة updatePagesRead
     */
    private static void updatePagesRead(Scanner scanner, BookRepository bookRepository) {

        // نسأل المستخدم عن رقم الكتاب
        int id = readInt(scanner, "رقم الكتاب (ID): ", false);

        // نسأل عن عدد الصفحات المقروءة الجديد (الإجمالي مش اللي اتقرأ النهاردة بس)
        int pagesRead = readInt(scanner, "عدد الصفحات المقروءة الجديد: ", false);

        try {
            // ننادي updatePagesRead: بترجع القيمة اللي اتخزنت فعلًا، أو -1 لو الكتاب مش موجود
            int stored = bookRepository.updatePagesRead(id, pagesRead);

            if (stored == -1) {
                System.out.println("لا يوجد كتاب بالرقم " + id + ".");
            } else if (stored < pagesRead) {
                // الرقم اللي كتبه المستخدم أكبر من صفحات الكتاب، فاتحط عدد الصفحات الكلي
                System.out.println("الرقم أكبر من عدد صفحات الكتاب، فتم تسجيل " + stored + " (كل الصفحات).");
            } else {
                System.out.println("تم التحديث: الصفحات المقروءة الآن " + stored + ".");
            }
        } catch (Exception e) {
            // لو حصلت مشكلة في الاتصال بالقاعدة نطبعها
            e.printStackTrace();
        }
    }

    /**
     * دالة بتسأل المستخدم عن نص البحث، وتعرض الكتب المطابقة (10 في كل صفحة)
     * وبتخلي المستخدم يتنقل بين صفحات النتايج.
     *
     * النص ممكن يكون العنوان أو جزء منه، أو اسم المؤلف أو جزء منه،
     * أو الاتنين مع بعض (مثلًا: clean martin).
     *
     * الأوامر جوه العرض:
     *   n → الصفحة التالية
     *   p → الصفحة السابقة
     *   b → الرجوع للقائمة الرئيسية
     *
     * @param scanner           الأداة اللي بنقرأ بيها من الكيبورد
     * @param libraryRepository الكائن اللي بننادي منه دالة search
     */
    private static void searchBooks(Scanner scanner, LibraryRepository libraryRepository) {

        // نسأل المستخدم عن نص البحث
        System.out.print("اكتب العنوان أو اسم المؤلف (أو جزء منهم): ");
        String text = scanner.nextLine().trim();

        // لو المستخدم مكتبش حاجة نرجع للقائمة (بدل ما نعرض كل الكتب، ده دور الاختيار l)
        if (text.isEmpty()) {
            System.out.println("لم تكتب نص للبحث.");
            return;
        }

        // رقم الصفحة الحالية من نتايج البحث، بنبدأ من الأولى
        int currentPage = 1;

        // نفضل نعرض صفحات النتايج لحد ما المستخدم يختار الرجوع
        while (true) {
            try {
                // نسأل القاعدة عن عدد صفحات نتايج البحث الكلي
                int totalPages = libraryRepository.getSearchTotalPages(text);

                // لو مفيش ولا نتيجة نبلغ المستخدم ونرجع للقائمة
                if (totalPages == 0) {
                    System.out.println("لا توجد كتب مطابقة للبحث.");
                    return;
                }

                // نتأكد إن رقم الصفحة الحالية جوه الحدود الصحيحة (من 1 لحد آخر صفحة)
                if (currentPage > totalPages) {
                    currentPage = totalPages;
                }
                if (currentPage < 1) {
                    currentPage = 1;
                }

                // نجيب نتايج الصفحة الحالية (10 كتب على الأكثر) باستخدام الدالة search
                List<Book> books = libraryRepository.search(text, currentPage);

                // نعرض رقم الصفحة وكل كتب الصفحة
                System.out.println("\n----- نتايج البحث: صفحة " + currentPage + " من " + totalPages + " -----");
                for (Book book : books) {
                    System.out.println(book);
                    System.out.println();
                }

            } catch (Exception e) {
                // لو حصلت مشكلة في الاتصال بالقاعدة نطبعها ونرجع للقائمة
                e.printStackTrace();
                return;
            }

            // نسأل المستخدم عايز يعمل إيه بعد كده
            System.out.print("n = التالية | p = السابقة | b = رجوع : ");
            String command = scanner.nextLine().trim().toLowerCase();

            if (command.equals("n")) {
                // الصفحة التالية (لو كنا في آخر صفحة، الكود فوق هيرجعنا لآخر صفحة)
                currentPage++;

            } else if (command.equals("p")) {
                // الصفحة السابقة (لو كنا في الأولى، الكود فوق هيخلينا فيها)
                currentPage--;

            } else if (command.equals("b")) {
                // الرجوع للقائمة الرئيسية
                return;

            } else {
                System.out.println("اختيار غير صحيح، اكتب n أو p أو b فقط.");
            }
        }
    }

    /**
     * دالة بتفضل تسأل المستخدم لحد ما يكتب رقم صحيح موجب.
     * بتمنع البرنامج إنه يقع لو المستخدم كتب حروف بدل أرقام.
     *
     * @param scanner  أداة القراءة من الكيبورد
     * @param message  الرسالة اللي هتظهر للمستخدم
     * @param optional لو true يبقى مسموح المستخدم يسيبها فاضية (وترجع صفر)
     * @return الرقم اللي كتبه المستخدم
     */
    private static int readInt(Scanner scanner, String message, boolean optional) {
        while (true) {
            System.out.print(message);
            String input = scanner.nextLine().trim();

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
}