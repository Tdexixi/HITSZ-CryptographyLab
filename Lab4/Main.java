import java.io.*;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static BigInteger p = null;
    public static BigInteger q = null;
    public static final Scanner input = new Scanner(System.in);
    public static SecureRandom random = new SecureRandom();
    public static BigInteger minusk,d;
    public static HashMap<Character, String>mymap = new HashMap<Character, String>();
    public static int maxlength = 0;
    public static BigInteger one = new BigInteger("1");
    public static BigInteger zero = new BigInteger("0");
    public static BigInteger two = new BigInteger("2");

    public static void main(String []args) throws IOException {
        BigInteger n = null;
        BigInteger euler_n;
        BigInteger e;
        boolean flag = false;
        BigInteger g = null;
        
        // 生成素数P
        p = generate_n();                                
        
        // 寻找生成元
        g = find_g(p);

        // 生成私钥x
        byte[] temp_byte = p.toByteArray();
        random.nextBytes(temp_byte);
        BigInteger x = new BigInteger(1,temp_byte);

        // 生成y
        BigInteger y;
        y = fast_count(g, x, p);

        System.out.println("公钥（y,p,g）为：\ny:" + y + "\np:" + p + "\ng:" + g);
        System.out.println("私钥为：" + x);

        // 消息
        String m = "200110112";
        
        for (int i = 1; i<3; i++) {
            System.out.println("======================================");
            System.out.println("第" +i + "次生成k");
            // 生成r
            BigInteger k;
            m = "200110112";
            do {
                random.nextBytes(temp_byte);
                k = new BigInteger(1, temp_byte);
            }
            while ((k.compareTo(one) == -1 || k.compareTo(p.subtract(one)) == 1) || ex_Euclid(k, p.subtract(one)).compareTo(one) != 0);
            BigInteger r = fast_count(g, k, p);
            System.out.println("k值为："+k);

            ex_Euclid(k, p.subtract(one));
            BigInteger s = minusk.multiply(new BigInteger(m).subtract(x.multiply(r))).mod(p.subtract(one));

            System.out.println("消息为m：" + m);
            System.out.println("签名为：(" + r + ",  " + s + ")");
            System.out.println("现在开始验证：");


            if (fast_count(y, r, p).multiply(fast_count(r, s, p)).mod(p).compareTo(
                    fast_count(g, new BigInteger(m), p)) == 0) {
                System.out.println("验证成功！");
            } else {
                System.out.println("FAIL!!验证失败!");
            }
            
            m = "200110114";
            System.out.println("现在将m更改为" + m);
            System.out.print("m篡改后的验证结果为：");
            if (fast_count(y, r, p).multiply(fast_count(r, s, p)).mod(p).compareTo(
                    fast_count(g, new BigInteger(m), p)) == 0) {
                System.out.println("验证成功！");
            } else {
                System.out.println("FAIL!!验证失败!");
            }
            
        }
    }


    //生成一个大素数P = 2q+1, q也为素数
    public static BigInteger generate_n(){

        BigInteger result = null;
        BigInteger a = two;
        BigInteger i = one;

        BigInteger R = one;
        int r = 1;

        BigInteger temp_q = null;

        byte bytes[] = new byte[1];

        while(result==null || !(fast_count(two, result.subtract(one), result).compareTo(one)==0 &&
                ex_Euclid((two.pow(2)).subtract(one), result).compareTo(one)==0)) {
            //生成R
            temp_q = null;
            r = random.nextInt(100);
            while (r % 2 != 0) {
                r = random.nextInt(100);
            }
            R = new BigInteger(String.valueOf(r));

            while (temp_q == null) {
                random.nextBytes(bytes);
                temp_q = new BigInteger(1, bytes);
                if (temp_q.and(one).compareTo(zero) == 0) {
                    temp_q = null;
                } else if (!temp_q.isProbablePrime(50)) {
                    temp_q = null;
                }
            }
            BigInteger qplus1 = temp_q;
            BigInteger tmp = null;
            int length = 0;
            do {
                tmp = two.multiply(R).multiply(temp_q);
                qplus1 = tmp.add(one);
                R = R.add(two);
                temp_q = qplus1;
                r = r + 2;
                length = qplus1.toByteArray().length;
            } while (!(fast_count(two, tmp, qplus1).compareTo(one) == 0 &&
                    ex_Euclid((a.pow(2 * (r-2))).subtract(one), qplus1).compareTo(one) == 0)&&length<128);
            result = (two.multiply(qplus1)).add(one);
        }
        return result;
    }

    // 寻找p的一个生成元
    public static BigInteger find_g(BigInteger p){
        BigInteger g = null;
        byte[] temp_bytes = p.toByteArray();
        do{
        	random.nextBytes(temp_bytes);
        	g = new BigInteger(1,temp_bytes);
        }while(g.compareTo(p.subtract(two))!=-1||g.mod(two).compareTo(one)!=0);
        return g;
    }


    //拓展欧几里得算法
    public static BigInteger ex_Euclid(BigInteger a,BigInteger b){
        if(b.compareTo(zero)==0){
            minusk = new BigInteger("1");
            d = new BigInteger("0");
            return a;
        }
        BigInteger result = ex_Euclid(b,a.mod(b));
        BigInteger temp = minusk;
        minusk = d;
        d = temp.subtract(a.divide(b).multiply(d));
        return result;
    }

   
    //初始化字母数字映射表,数字字符映射到10-19，小写字母映射到20-45，大写字母映射到46-71
    public static void mapinit(){
        int i = 20;
        int j = 10;
        char letter[] = {'a','b','c','d','e','f','g','h','i','j',
                'k','l','m','n','o','p','q','r','s','t',
                'u','v','w','x','y','z'};
        char digit[] = {'0','1','2','3','4','5','6','7','8','9'};
        for(char c:letter){
            mymap.put(c,String.valueOf(i));
            mymap.put(Character.toUpperCase(c),String.valueOf(i+26));
            i++;
        }
        for(char d:digit){
            mymap.put(d,String.valueOf(j));
            j++;
        }
    }

    //快速幂
    public static BigInteger fast_count(BigInteger a, BigInteger b, BigInteger n){
        BigInteger y  = new BigInteger("1");
        BigInteger temp_a = a;
        BigInteger temp_b = b;
        BigInteger temp_n = n;
        while(true){
            if(temp_b.compareTo(zero)==0){
                return y;
            }
            while(temp_b.compareTo(zero)==1&&temp_b.mod(two).compareTo(zero)==0){
                temp_a = (temp_a.multiply(temp_a)).mod(temp_n);
                temp_b = temp_b.divide(two);
            }
            temp_b = temp_b.subtract(one);
            y = (temp_a.multiply(y)).mod(temp_n);
        }
    }

    // Miller-Robin法素性检测
    public static boolean millerRobin(BigInteger b, int round){
        BigInteger temp_b = b.subtract(one);
        BigInteger di_num = temp_b;
        int count = 0;
        BigInteger temp_q=null;
        do{
            if(di_num.and(one).compareTo(one)==0){
                temp_q = di_num;
                break;
            }
            di_num = di_num.divide(two);
            count  = count + 1;
        }while(true);
        for (int k=0; k<round; k++) {
            int temp = random.nextInt(256);           // 生成一个随机数，用来生成大素数的位数
            BigInteger a = new BigInteger(temp, random);         // 生成位数为temp的随机数a
            if (fast_count(a, temp_q, b).compareTo(one) == 0 || fast_count(a, temp_q, b).compareTo(temp_b) == 0) {
                for (int j = 1; j < count; j++) {
                    if (fast_count(a, (two.pow(j)).multiply(temp_q), b).compareTo(temp_b) != 0) {
                        return false;
                    }
                }
            }else{
                return false;
            }
        }
        return true;
    }

}

