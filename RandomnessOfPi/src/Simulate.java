import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Simulate {
    static ArrayList<Integer> arr = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        // Accessing Resources
        String path = "C:\\Users\\keert\\IdeaProjects\\RandomnessOfPi\\src\\digits"; // absolute path of digits.txt
        try (Reader reader = Files.newBufferedReader(Path.of(path))) {
            int c;
            while ((c = reader.read()) != -1) {
                char ch = (char) c;
                int value = Character.getNumericValue(ch);
                if (value >= 0) {
                    arr.add(value);
                }
            }
        }
        // Useless Statistics
        System.out.println("Distribution of digits (rows) that follow a each digit (columns)");
        ArrayList<ArrayList<Double>> n_pi = determineTransitionMatrix();
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                System.out.print(n_pi.get(i).get(j) + "   ");
            }
            System.out.println("\n");
        }

        // Actual Simulation
        System.out.println("Simulate Sum \nmean: " + simulateSum(1000, 10)[0] + " variance: " + simulateSum(1000, 10)[1]);
        System.out.println("Simulate Coupon Collector \nmean: " + simulateCouponCollector(100)[0] + " variance: " + simulateCouponCollector(100)[1]);
        System.out.println("Simulate Birthday Problem \nmean: " + simulateBirthdays(3, 100)[0] + " variance: " + simulateBirthdays(3, 100)[1]);
        System.out.println("Simulate Banach's Matchbox Problem \nmean: " + simulateBanachMatchbox(30, 1000)[0] + " variance: " + simulateBanachMatchbox(30, 1000)[1]);
        System.out.println("Simulate Runs \nmean: " + simulateConsecutiveRuns()[0] + " variance: " + simulateConsecutiveRuns()[1]);
    }

    /**
     * Determines a "transition matrix" for the digits of PI
     *
     * @return
     */
    public static ArrayList<ArrayList<Double>> determineTransitionMatrix() {
        ArrayList<ArrayList<Double>> mat = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            ArrayList<Double> r = new ArrayList<>();
            for (int j = 0; j < 10; j++) {
                r.add(0d);
            }
            mat.add(r);
        }
        for (int i = 0; i < arr.size() - 1; i++) {
            ArrayList<Double> ls = mat.get(arr.get(i));
            ls.set(arr.get(i + 1), ls.get(arr.get(i + 1)) + 1);
            mat.set(arr.get(i), ls);
        }

        for (int i = 0; i < 10; i++) {
            double sum = 0;
            for (int j = 0; j < 10; j++) {
                sum += mat.get(i).get(j);
            }
            ArrayList<Double> ls = mat.get(i);
            for (int j = 0; j < 10; j++) {
                ls.set(j, ls.get(j)/sum);
            }
            mat.set(i, ls);
        }
        return mat;
    }

    /**
     * Simulates a random variable that takes the sum of n numbers over k iterations
     *
     * @param n range
     * @param k trials
     * @return double[] {mean, variance}
     */
    public static double[] simulateSum(int n, int k) {
        ArrayList<Double> brr = new ArrayList<>();
        for (int x = 0; x < k; x++) {
            double r = 0;
            for (int i = 0; i < n; i++) {
                int l = (int) (Math.random() * arr.size());
                r += (double) arr.get(l);
            }
            brr.add(r);
        }
        return new double[]{calculateMean(brr), calculateVariance(brr)};
    }

    /**
     * Simulates the procedure of the coupon collector problem, where each digit represents a coupon type.
     * A random starting point will be chosen and iteration will cyclically proceed until all 10 coupons are collected.
     * The length of the number of pickings will be analyzed.
     *
     * @param n (number of experimental trials)
     * @return double[] {mean, variance}
     */
    public static double[] simulateCouponCollector(int n) {
        ArrayList<Double> brr = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            int x, l;
            x = l = (int) (Math.random() * arr.size());
            Set<Integer> hs = new HashSet<>();
            while (hs.size() < 10) {
                hs.add(arr.get(l % arr.size()));
                l++;
            }
            brr.add((double) (l - x));
        }
        return new double[]{calculateMean(brr), calculateVariance(brr)};
    }

    /**
     * Simulates the Birthday problem where n represents the length of the consecutive substring taken from the digits of pi.
     * Computes the mean and variance over k trials of the number of birthdays needed to have more than 50% of the days containing 2 or more birthdays.
     *
     * @param n substring length of digits
     * @param k number of trials
     * @return double[] {mean, variance}
     */
    public static double[] simulateBirthdays(int n, int k) {
        ArrayList<Double> brr = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            int x = (int) (Math.random() * arr.size());
            ArrayList<Double> crr = new ArrayList<>();
            for (int j = x; j < arr.size() * n; j += n) {
                // populate birthdays
                int y = 0;
                for (int l = 0; l < n; l++) {
                    y += arr.get((l + j) % arr.size()) * Math.pow(10, n - l - 1);
                }
                crr.add((double) y);
                Collections.sort(crr);
                // count duplicates
                double r = 0, temp = 0;
                for (int t = 1; t < crr.size(); t++) {
                    if (Objects.equals(crr.get(t), crr.get(t - 1))) {
                        if (crr.get(t) != temp) {
                            r++;
                            temp = crr.get(t);
                        } else {
                            continue;
                        }
                    }
                }
                // end case
                if (r > Math.pow(10, n) / 2) {
                    brr.add((double) crr.size());
                    break;
                }
            }
        }
        return new double[]{calculateMean(brr), calculateVariance(brr)};
    }

    /**
     * Simulates procedure for Banach's Matchbox problem
     *
     * @param n (number of start matches)
     * @param m (number of trials)
     * @return double[] {mean, variance}
     */
    public static double[] simulateBanachMatchbox(int n, int m) {
        ArrayList<Double> brr = new ArrayList<>();
        for (int i = 0; i < m; i++) {
            int a = n, b = n;
            while (a > 0 && b > 0) {
                int x = (int) (Math.random() * arr.size());
                if (arr.get(x % arr.size()) >= 5) {
                    b--;
                } else {
                    a--;
                }
            }
            brr.add((double) Math.max(a, b));
        }
        return new double[]{calculateMean(brr), calculateVariance(brr)};
    }

    /**
     * Simulates the number of consecutive digits there are in pi
     *
     * @return double[] {mean, (variance)}
     */
    public static double[] simulateConsecutiveRuns() {
        ArrayList<Double> brr = new ArrayList<>();
        int x = 1;
        for (int i = 0; i < arr.size() - 1; i++) {
            if (arr.get(i) == arr.get(i + 1)) {
                x++;
            } else {
                brr.add((double) x);
                x = 1;
            }
        }
        return new double[]{calculateMean(brr), calculateVariance(brr)};
    }

    /**
     * Calculates mean of a list of Doubles
     *
     * @param brr
     * @return mean
     */
    public static double calculateMean(List<Double> brr) {
        double n = 0;
        for (int i = 0; i < brr.size(); i++) {
            n += brr.get(i);
        }
        return n / brr.size();
    }

    /**
     * Calculates variance of a list of Doubles
     *
     * @param brr
     * @return variance
     */
    public static double calculateVariance(List<Double> brr) {
        double x = calculateMean(brr), y = 0;
        for (int i = 0; i < brr.size(); i++) {
            y += Math.pow(brr.get(i) - x, 2);
        }
        return y / brr.size();
    }
}
