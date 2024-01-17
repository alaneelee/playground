package com.example.playground;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class MemoryDiskReadWriteTests {

    // 리스트 자료구조에 데이터를 100만번 쓰기/읽기 테스트
    @Test
    public void memoryTest() {
        String sample = "This is sample string";
        List<String> arrayList = new ArrayList<>();

        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            arrayList.add(sample);
        }

        new ArrayList<>(arrayList);

        long end = System.currentTimeMillis();
        System.out.println("Execution time: " + (end - start) + "ms");
    }

    // 디스크에 100만번 쓰기/읽기 테스트
    @Test
    public void diskTest() {
        final var filename = "clip01.txt";
        final var sample = "this is sample string";
        File file = new File(filename);

        long start = System.currentTimeMillis();

        try {
            OutputStream os = new FileOutputStream(file);
            PrintWriter writer = new PrintWriter(os);

            for (int i = 0; i < 1000000; i++) {
                writer.println(sample);
            }

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Scanner scanner = new Scanner(new File(filename));
            while (scanner.hasNextLine()) {
                scanner.nextLine();
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        long end = System.currentTimeMillis();
        System.out.println("Execution time: " + (end - start) + " ms");
    }

}
