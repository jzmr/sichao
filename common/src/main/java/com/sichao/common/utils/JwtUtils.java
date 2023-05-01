package com.sichao.common.utils;

import com.sichao.common.constant.Constant;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * jwt 前两部分是对 header 以及 payload 的 base64 编码。 当服务器收到客户端的 token 后,
 * 解析前两部分得到 header 以及 payload，并使用 header 中的算法与 服务端本地私有 secret
 * 进行签名，判断与 jwt 中携带的签名是否一致.
 *
 * 有效载荷里的信息，在创建token时可以设置
 * iss (issuer)：签发人
 * exp (expiration time)：过期时间
 * sub (subject)：主题
 * aud (audience)：受众
 * nbf (Not Before)：生效时间
 * iat (Issued At)：签发时间
 * jti (JWT ID)：编号
 */
//JWT工具类
@Slf4j
public class JwtUtils {

    public static final long EXPIRE = 1000 * 60 * 60 * 24;//token的过期时间：1天

    public static final String APP_SECRET = Constant.TOKEN_SECRET_KEY;//秘钥（每个公司都会各自生成不同的秘钥）

    //传入主体部分，设置过期时间为1天，生成token字符串的方法,
    public static String getJwtToken(String id, String nickname){

        String JwtToken = Jwts.builder()
                //设置token的头信息（一般不改） (使用 Base64URL 算法将头信息JSON对象转成字符串)
                .setHeaderParam("alg", "HS256")//表示签名的算法，默认是HMAC SHA256(写成 HS256)
                .setHeaderParam("typ", "JWT")//表示这个令牌(token)的类型(type)，JWT令牌统一写为 JWT。
                //设置过期时间
                .setSubject("sichao-user")  //主题
                .setIssuedAt(new Date())    //签发时间
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRE))   //过期时间
                //设置token的主体部分，存储用户信息
                .claim("id", id)
                .claim("nickname", nickname)
                //设置签名哈希（一般不改）
                .signWith(SignatureAlgorithm.HS256, APP_SECRET)
                .compact();

        return JwtToken;
    }
    //传入主体部分与过期时间，单位为毫秒，生成token字符串的方法
    public static String getJwtToken(String id, String nickname,long expireTime){

        String JwtToken = Jwts.builder()
                //设置token的头信息（一般不改）
                .setHeaderParam("alg", "HS256")
                .setHeaderParam("typ", "JWT")
                //设置过期时间
                .setSubject("sichao-user")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expireTime))
                //设置token的主体部分，存储用户信息
                .claim("id", id)
                .claim("nickname", nickname)
                //设置签名哈希（一般不改）
                .signWith(SignatureAlgorithm.HS256, APP_SECRET)
                .compact();

        return JwtToken;
    }

    /**
     * 判断token是否存在与有效
     * @param jwtToken
     * @return
     */
    public static boolean checkToken(String jwtToken) {
        if(StringUtils.isEmpty(jwtToken)) return false;
        try {//根据秘钥判断传入的token是否是有效的token
            Jwts.parser().setSigningKey(APP_SECRET).parseClaimsJws(jwtToken);
        } catch (Exception e) {
            log.info("该token已失效");
            return false;
        }
        return true;
    }

    /**
     * 根据请求携带的token判断是否存在与有效
     * @param request
     * @return
     */
    public static boolean checkToken(HttpServletRequest request) {
        try {
            String jwtToken = request.getHeader(Constant.TOKEN);
            if(StringUtils.isEmpty(jwtToken)) return false;
            Jwts.parser().setSigningKey(APP_SECRET).parseClaimsJws(jwtToken);
        } catch (Exception e) {
            log.info("该token已失效");
            return false;
        }
        return true;
    }

    /**
     * 根据请求携带的token获取用户id（用户信息）
     * @param request
     * @return
     */
    public static String getUserIdByJwtToken(HttpServletRequest request) {
        String jwtToken = request.getHeader(Constant.TOKEN);
        if(StringUtils.isEmpty(jwtToken)) return "";
        Jws<Claims> claimsJws=null;
        try {
            claimsJws = Jwts.parser().setSigningKey(APP_SECRET).parseClaimsJws(jwtToken);
        }catch (Exception e) {
            log.info("该token已失效");
            return null;
        }
        Claims claims = claimsJws.getBody();//得到主体部分
        return (String)claims.get("id");
    }

    /**
     * 根据token获取用户id（用户信息）
     * @param jwtToken
     * @return
     */
    public static String getUserIdByJwtToken(String jwtToken) {
        if(StringUtils.isEmpty(jwtToken)) return "";

        Jws<Claims> claimsJws=null;
        try {
            claimsJws = Jwts.parser().setSigningKey(APP_SECRET).parseClaimsJws(jwtToken);
        }catch (Exception e) {
            log.info("该token已失效");
            return null;
        }
        Claims claims = claimsJws.getBody();//得到主体部分
        return (String)claims.get("id");
    }
    /**
     * 根据token获取用户昵称（用户信息）
     * @param jwtToken
     * @return
     */
    public static String getNicknameByJwtToken(String jwtToken) {
        if(StringUtils.isEmpty(jwtToken)) return "";

        Jws<Claims> claimsJws=null;
        try {
            claimsJws = Jwts.parser().setSigningKey(APP_SECRET).parseClaimsJws(jwtToken);
        }catch (Exception e) {
            log.info("该token已失效");
            return null;
        }
        Claims claims = claimsJws.getBody();//得到主体部分
        return (String)claims.get("nickname");
    }


    //获取token的剩余生存时间
    public static long getTokenTTL(String jwtToken){
        if(StringUtils.isEmpty(jwtToken))return 0;
        Jws<Claims> claimsJws=null;
        try {
            claimsJws = Jwts.parser().setSigningKey(APP_SECRET).parseClaimsJws(jwtToken);
        }catch (Exception e) {
            log.info("该token已失效");
            return 0;
        }
        Claims claims = claimsJws.getBody();//得到主体部分(有效载荷)
        Date iat = claims.getIssuedAt();//签发时间
        Date exp = claims.getExpiration();//过期时间
        long tokenTTL = exp.getTime() - iat.getTime();//单位：毫秒
        return tokenTTL;
    }

}
