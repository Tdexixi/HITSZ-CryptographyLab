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
    public static BigInteger d,y;
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
        n = generate_n();                                   // 寻找p,q，乘积为n
        BigInteger one = new BigInteger("1");
        euler_n = (p.subtract(one)).multiply(q.subtract(one));  // 欧拉函数
        int temp = random.nextInt(512)+512;           // 生成一个随机数，用来生成大素数的位数
        e = new BigInteger(temp,50,random);         // 生成位数为temp的随机大素数e
        while(!flag) {
            if (e.compareTo(euler_n) == -1 && e.compareTo(one) == 1 && ex_Euclid(e,euler_n).compareTo(one) == 0) {
                flag = true;
                if(d.compareTo(new BigInteger("0"))==-1){           // 如果d为负数，则加上euler_n
                    d = d.add(euler_n);
                }
            }
            else{
                temp = random.nextInt(512)+512;
                e = new BigInteger(temp,50,random);
            }
        }
        System.out.println("The first prime p is:\n"+p);
        System.out.println("The Second prime q is:\n"+q);
        System.out.println("n = p x q is：\n"+n);
        System.out.println("The Euler fuction euler_n is:\n"+euler_n);
        System.out.println("The public key e is:\n"+e);
        System.out.println("The private key d is:\n"+d);
        rsa(e,n);
        deRsa(d,n);
    }


    //求两个大素数积
    public static BigInteger generate_n(){
        BigInteger result = null;
        int temp = 0;

        //生成两个512位的随机数，然后进行素性检测
        byte bytes[] = new byte[64];
        while(p==null ) {
            random.nextBytes(bytes);
            p = new BigInteger(1,bytes);
            if(p.and(one).compareTo(zero)==0){
                p = null;
            }
            else if(!millerRobin(p,50)){
                p = null;
            }
        }
        while(q==null){
            random.nextBytes(bytes);
            q = new BigInteger(1,bytes);
            if(q.and(one).compareTo(zero)==0){
                q = null;
            }
            else if(!millerRobin(q,50)){
                q = null;
            }
        }
        result = p.multiply(q);
        return result;
    }


    //拓展欧几里得算法
    public static BigInteger ex_Euclid(BigInteger a,BigInteger b){
        if(b.compareTo(zero)==0){
            d = new BigInteger("1");
            y = new BigInteger("0");
            return a;
        }
        BigInteger result = ex_Euclid(b,a.mod(b));
        BigInteger temp = d;
        d = y;
        y = temp.subtract(a.divide(b).multiply(y));
        return result;
    }

    //加密过程
    public static void rsa(BigInteger e, BigInteger n) throws IOException {
        System.out.println("Please input the file you want to encrypt: (ex:lab2-Plaintext.txt)");
        String path = input.nextLine();
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
        String strtmp;
        String pro_str = "[^0-9A-Za-z]";
        StringBuilder temp_line = new StringBuilder("");
        List<String> lines = new ArrayList<>();
        List<String> pr_lines = new ArrayList<>();
        mapinit();
        //去除字符串中的特殊字符
        while((strtmp=reader.readLine())!=null){
            strtmp = strtmp.replaceAll(pro_str,"");
            lines.add(strtmp);
        }
        reader.close();
        //将字符转换成数字
        for(String line:lines){
            for(int k=0;k<line.length();k++){
                temp_line.append(mymap.get(line.charAt(k)));
            }
            pr_lines.add(temp_line.toString());
            temp_line.delete(0,temp_line.length());
        }
        // 分组
        List<BigInteger> group = new ArrayList<>();
        for(String line:pr_lines){
            int beginIndex = 0;
            int endIndex = 4;
            while(beginIndex<line.length()){
                if(endIndex<line.length()){      // 不需要补0
                    group.add(BigInteger.valueOf(Integer.parseInt(line.substring(beginIndex,endIndex))));
                    beginIndex+=4;
                    endIndex += 4;
                }
                else{                           // 补零
                    group.add(BigInteger.valueOf(Integer.parseInt
                            (line.substring(beginIndex,beginIndex+2))* 100L +72));
                    beginIndex+=4;
                    endIndex += 4;
                }
            }
        }

        //计算每一组密文
        List<BigInteger> Cgroup = new ArrayList<>();
        for(BigInteger x:group){
            Cgroup.add(fast_count(x,e,n));
        }


        //将每一组密文转换成字符串
        List<String> Cstring = new ArrayList<>();

        for(BigInteger x:Cgroup){
            Cstring.add(x.toString());
            if(maxlength<x.toString().length()){
                maxlength = x.toString().length();
            }
        }
        System.out.println("The length of each ciphertext packet is:\n"+maxlength);

        //为每一组不够最长长度的密文补零
        for(int i=0;i<Cstring.size();i++){
            String x = Cstring.get(i);
            if(x.length()<maxlength){
                StringBuilder appe_str = new StringBuilder(x);
                for(int temp = 0;temp<maxlength-x.length();temp++){
                    appe_str.insert(0,"0");
                }
                Cstring.set(i,appe_str.toString());
            }
        }

        //将密文写入文件
        System.out.println("Which file does the ciphertext need to be output to? Please enter: (ex:a.txt)");
        String path_out = input.nextLine();
        BufferedWriter writer;
        try{
            writer = new BufferedWriter(new FileWriter(path_out));
            for (String x:Cstring){
                writer.write(x);
            }
            writer.flush();
            writer.close();
        }catch (IOException exception){
            exception.printStackTrace();
        }


    }

    //解密过程
    public static void deRsa(BigInteger de,BigInteger n) throws IOException {
        System.out.println("Please enter the file you want to decrypt: (ex:a.txt)");
        String path_in = input.nextLine();
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path_in)));
        List<String> input_C = new ArrayList<>();
        List<BigInteger> de_nums= new ArrayList<>();
        String str_tmp;
        while((str_tmp = br.readLine())!=null){
            String x;
            int beginIndex = 0;
            int endIndex = maxlength;
            while(beginIndex<str_tmp.length()){
                x = str_tmp.substring(beginIndex,endIndex);
                input_C.add(x);
                beginIndex += maxlength;
                endIndex += maxlength;
            }

        }
        for (String x:input_C){
            de_nums.add(new BigInteger(x));
        }
        for(int i=0;i<de_nums.size();i++){
            de_nums.set(i,fast_count(de_nums.get(i),de,n));
        }
        List<String> M_num =new ArrayList<>();
        for (BigInteger x:de_nums){
            M_num.add(x.toString());
        }
        StringBuffer sb = new StringBuffer("");
        String two_char;
        for (String x:M_num){
            for(int i=0; i<4; i+=2){
                two_char = x.substring(i,i+2);
                for (char key: mymap.keySet()){
                    if(mymap.get(key).equals(two_char)){
                        sb.append(key);
                    }
                    else{
                        sb.append("");
                    }
                }
            }
        }
        String final_plaintext = sb.toString();
        System.out.println("The plaintext obtained by decryption is: \n"+final_plaintext);

        //输出明文到文件
        System.out.println("Please enter the file the plaintext should output to: (ex:a.txt)");
        String p_out_path = input.nextLine();
        BufferedWriter writer;
        try{
            writer = new BufferedWriter(new FileWriter(p_out_path));
            writer.write(final_plaintext);
            writer.flush();
            writer.close();
        }catch (IOException exception){
            exception.printStackTrace();
        }
        System.out.println("Decryption complete!");
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

