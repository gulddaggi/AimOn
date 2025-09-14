// package com.example.ffp_be.config;

// import io.github.cdimascio.dotenv.Dotenv;
// import org.springframework.context.annotation.Configuration;

// @Configuration
// public class EnvConfig {

//     public static void loadEnv() {

//         Dotenv dotenv = Dotenv.configure()
//             .directory("../../")
//             .filename("env.dev")
//             .ignoreIfMalformed()
//             .ignoreIfMissing()
//             .load();

//         System.setProperty("DB_USERNAME", dotenv.get("DB_USERNAME"));
//         System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));
//     }

// }