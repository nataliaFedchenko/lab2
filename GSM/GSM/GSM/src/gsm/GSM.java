package gsm;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class GSM {

    private static int calls = 100;
    private static int towers = 50;
    private static int threads = 15;
    private static Call callsArr[] = new Call[calls];
    private static Tower towersArr[] = new Tower[towers];
    private static int numberCalls[] = new int[towers];
    private static int time = 10;

    public static void main(String[] args) {
        generateBinFiles();

        try {
            
            long before = System.currentTimeMillis();
            Thread t1 = new Thread(new CallsFromFile());
            Thread t2 = new Thread(new TowersFromFile());
            t1.start();
            t2.start();

            Thread t3[] = new Thread[threads - 2];

            int loops_count = calls / (threads - 2);
            int last_loop = calls - calls / (threads - 2) * (threads - 3);
            for (int t = 0; t < threads - 2; t++) {
                int loop;
                if (t != threads - 3) {
                    loop = loops_count;
                } else {
                    loop = last_loop;
                }
                for (int i = 0; i < loop; i++) {
                    for (int j = 0; j < towers; j++) {
                        t3[t] = new Thread(new CalcInAreaCalls(callsArr[t * loops_count + i], towersArr[j], j));
                        t3[t].start();
                    }
                }
                t3[t].join();
            }

            t1.join();
            t2.join();
            long after = System.currentTimeMillis();
            System.out.println("time = " + (after - before) + " ms");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        for (int i = 0; i < towers; i++) {
            System.out.println(numberCalls[i]);
        }
    }

    private static void generateBinFiles() {
        try {
            Random rand = new Random();
            for (int i = 0; i < calls; i++) {
                Path file = Paths.get("src", String.valueOf(i) + "c.bin");
                byte[] buf = {(byte) rand.nextInt(10), (byte) rand.nextInt(10), (byte) rand.nextInt(10), (byte) rand.nextInt(10)};
                Files.write(file, buf);
            }
            for (int i = 0; i < towers; i++) {
                Path file = Paths.get("src", String.valueOf(i) + "t.bin");
                byte[] buf = {(byte) rand.nextInt(10), (byte) rand.nextInt(10), (byte) rand.nextInt(10), (byte) rand.nextInt(10),
                    (byte) (rand.nextInt(4) + 1), (byte) (rand.nextInt(4) + 1)};
                Files.write(file, buf);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static class CalcInAreaCalls extends Thread {

        public CalcInAreaCalls(Call call, Tower tower, int i) {
            this.call = call;
            this.tower = tower;
            this.i = i;
        }
        Call call;
        Tower tower;
        int i;

        @Override
        public void run() {
            try {
                synchronized (numberCalls) {
                    if (tower == null || call == null)
                        sleep(time * 10);
                     if (tower != null && call != null) {

                            if ((tower.x - tower.r) <= call.x && call.x <= (tower.x + tower.r)) {
                                numberCalls[i] += 1;
                            }
                        }
                    
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private static class CallsFromFile extends Thread {

        @Override
        public void run() {
            try {
                for (int i = 0; i < calls; i++) {
                    sleep(time);
                    Path file = Paths.get("src", String.valueOf(i) + "c.bin");
                    byte[] fileArray;
                    fileArray = Files.readAllBytes(file);
                    callsArr[i] = new Call();
                    callsArr[i].x = ((fileArray[0] & 0xFF) << 8) + ((fileArray[1] & 0xFF));
                    callsArr[i].y = ((fileArray[2] & 0xFF) << 8) + (fileArray[3] & 0xFF);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private static class TowersFromFile extends Thread {

        @Override
        public void run() {
            try {
                
                for (int i = 0; i < towers; i++) {
                    sleep(time);
                    Path file = Paths.get("src", String.valueOf(i) + "t.bin");
                    byte[] fileArray;
                    fileArray = Files.readAllBytes(file);
                    towersArr[i] = new Tower();
                    towersArr[i].x = ((fileArray[0] & 0xFF) << 8) + ((fileArray[1] & 0xFF));
                    towersArr[i].y = ((fileArray[2] & 0xFF) << 8) + (fileArray[3] & 0xFF);
                    towersArr[i].r = ((fileArray[4] & 0xFF) << 8) + (fileArray[5] & 0xFF);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
