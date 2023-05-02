package com.sichao.common.utils;

//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * 加盐MD5加密类
 * 只要明文相同，那么生成的MD5码就相同，于是攻击者就可以通过撞库的方式来破解出明文。
 * 加盐就是向明文中加入随机数，然后在生成MD5，这样一来即使明文相同，但由于随机数是不
 * 同（极少相同），所以每次生成的MD5码也不同，如此一来就大大增加了暴力破解的难度，使
 * 其几乎不可能破解。
 */
public final class MD5 {

    //生成普通的MD5码
    public static String encrypt(String strSrc) {
        try {
            char hexChars[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
                    '9', 'a', 'b', 'c', 'd', 'e', 'f' };
            byte[] bytes = strSrc.getBytes();
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(bytes);
            bytes = md.digest();
            int j = bytes.length;
            char[] chars = new char[j * 2];
            int k = 0;
            for (int i = 0; i < bytes.length; i++) {
                byte b = bytes[i];
                chars[k++] = hexChars[b >>> 4 & 0xf];
                chars[k++] = hexChars[b & 0xf];
            }
            return new String(chars);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException("MD5加密出错！！+" + e);
        }
    }
    //生成“盐”和加盐后的MD5码，并将盐混入到MD5码中。（MD5加盐值防暴力破解）
    public static String saltEncryption(String password) {
        //生成一个16位的随机数，也就是所谓的“盐”
        Random r = new Random();
        StringBuilder sb = new StringBuilder(16);
        sb.append(r.nextInt(99999999)).append(r.nextInt(99999999));
        int len = sb.length();
        if (len < 16) {
            for (int i = 0; i < 16 - len; i++) {
                sb.append("0");
            }
        }
        String salt = sb.toString();
        //将“盐”加到明文中，并生成新的MD5码
        password = encrypt(password + salt);
        //将“盐”混到新生成的MD5码中，之所以这样做是为了后期更方便的校验明文和秘文，也可以不用这么做，不过要将“盐”单独存下来，不推荐这种方式
        char[] cs = new char[48];
        for (int i = 0; i < 48; i += 3) {
            cs[i] = password.charAt(i / 3 * 2);
            char c = salt.charAt(i / 3);
            cs[i + 1] = c;
            cs[i + 2] = password.charAt(i / 3 * 2 + 1);
        }
        return new String(cs);
    }

    //验证明文和加盐后的MD5码是否匹配,password为加密的密码，md5为以加盐MD5加密的密码
    public static boolean verify(String password, String md5) {
        //先从MD5码中取出之前加的“盐”和加“盐”后生成的MD5码
        char[] cs1 = new char[32];
        char[] cs2 = new char[16];
        for (int i = 0; i < 48; i += 3) {
            cs1[i / 3 * 2] = md5.charAt(i);
            cs1[i / 3 * 2 + 1] = md5.charAt(i + 2);
            cs2[i / 3] = md5.charAt(i + 1);
        }
        String salt = new String(cs2);
        //比较二者是否相同
        return encrypt(password + salt).equals(new String(cs1));
    }


//    //MD5加盐值防暴力破解
//    public static String saltEncryption(String strSrc) {
//        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
//        return passwordEncoder.encode(strSrc);//加盐值加密
//    }

    public static void main(String[] args) {
//        System.out.println(MD5.encrypt("111111"));//111111->96e79218965eb72c92a549dd5a330112
//        //MD5加盐值防暴力破解
//        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
//        String encode = passwordEncoder.encode("111111");//加盐值加密
//        System.out.println(encode);//$2a$10$mCWxPLcC8aYmL17Sq9nX3erbAU4CB5mASdZvOBmTB46VonPB8joiG
//        boolean matches = passwordEncoder.matches("111111", encode);//对比是否是同一个密码
//        System.out.println(matches);//true


        System.out.println(MD5.encrypt("111111"));
        //MD5加盐值防暴力破解
        String md5Password = MD5.saltEncryption("111111");
        System.out.println(md5Password);
        System.out.println(verify("111111",md5Password));


    }

}
