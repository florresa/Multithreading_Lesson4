import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class Main {
    public static void main(String[] args) throws InterruptedException {

        BlockingQueue<String> queue1 = new ArrayBlockingQueue<>(100);
        BlockingQueue<String> queue2 = new ArrayBlockingQueue<>(100);
        BlockingQueue<String> queue3 = new ArrayBlockingQueue<>(100);

        final AtomicLong aMaxCounter = new AtomicLong();
        final AtomicReference<String> aMaxString = new AtomicReference<>("");

        final AtomicLong bMaxCounter = new AtomicLong();
        final AtomicReference<String> bMaxString = new AtomicReference<>("");

        final AtomicLong cMaxCounter = new AtomicLong();
        final AtomicReference<String> cMaxString = new AtomicReference<>("");

        Thread thread1 = new Thread(() -> {
            for (int i = 0; i < 10_000; i++) {
                String text = generateText("abc", 100_000);
                try {
                    queue1.put(text);
                    queue2.put(text);
                    queue3.put(text);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        thread1.start();


        Thread thread2 = new Thread(() -> {
            findMaxString(queue1, 'a', aMaxCounter, aMaxString);
        });
        thread2.start();


        Thread thread3 = new Thread(() -> {
            findMaxString(queue2, 'b', bMaxCounter, bMaxString);
        });
        thread3.start();


        Thread thread4 = new Thread(() -> {
            findMaxString(queue3, 'c', cMaxCounter, cMaxString);
        });
        thread4.start();

        thread1.join();
        thread2.join();
        thread3.join();
        thread4.join();

        System.out.println("Найдена строка с максимальным количеством букв a: " + aMaxCounter.get());
        System.out.println(aMaxString.get());
        System.out.println("Найдена строка с максимальным количеством букв b: " + bMaxCounter.get());
        System.out.println(bMaxString.get());
        System.out.println("Найдена строка с максимальным количеством букв c: " + cMaxCounter.get());
        System.out.println(cMaxString.get());

    }

    public static long countLetter(char letter, String text) {
        return text.chars().filter(c -> c == letter).count();
    }

    public static void findMaxString(BlockingQueue<String> queue, char letter, AtomicLong maxCounter, AtomicReference<String> maxString) {
        while (true) {
            try {
                String text = queue.poll(5, TimeUnit.SECONDS);
                if (text != null) {
                    long count = countLetter(letter, text);
                    if (count > maxCounter.get()) {
                        maxCounter.set(count);
                        maxString.set(text);
                    }
                } else {
                    break;
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static String generateText(String letters, int length) {
        Random random = new Random();
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < length; i++) {
            text.append(letters.charAt(random.nextInt(letters.length())));
        }
        return text.toString();
    }
}
