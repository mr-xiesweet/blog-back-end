package top.xie;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestPasswordEncoder {
    public static void main(String[] args) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encode = passwordEncoder.encode("123456");
        System.out.println(encode);
//$2a$10$1zR6WE4JvcDggZGtbLqekee0ng68ijTwESm4vLOfkm0NBO0X39.Oi

        String originalPassword = "123456";
        boolean matches = passwordEncoder.matches(originalPassword, "$2a$10$1zR6WE4JvcDggZGtbLqekee0ng68ijTwESm4vLOfkm0NBO0X39.Oi");
        System.out.println(matches);
    }
}
