package com.wenhua.tcpservice.deep;

import java.util.concurrent.atomic.AtomicReference;

public class ThreadSafeStringStorage {
    // 使用 AtomicReference 存储字符串，保证原子性和可见性
    private final AtomicReference<String> storage = new AtomicReference<>("");

    /**
     * 覆盖写入字符串（原子操作）
     * @param newValue 新字符串
     */
    public void set(String newValue) {
        storage.set(newValue);
    }

    /**
     * 追加字符串（线程安全）
     * @param appendValue 要追加的字符串
     */
    public void append(String appendValue) {
        // 使用 CAS 循环确保原子性
        while (true) {
            String current = storage.get();
            String newValue = current + appendValue;
            if (storage.compareAndSet(current, newValue)) {
                break; // 更新成功则退出循环
            }
            // 更新失败则重试（其他线程已修改值）
        }
    }

    /**
     * 获取当前字符串（线程安全）
     * @return 当前存储的字符串
     */
    public String get() {
        return storage.get();
    }

    // 测试用例
    public static void main(String[] args) throws InterruptedException {
        ThreadSafeStringStorage storage = new ThreadSafeStringStorage();

        // 线程1：覆盖写入
        new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                storage.set("Set-" + i);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();

        // 线程2：追加写入
        new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                storage.append("Append-" + i + " ");
                try {
                    Thread.sleep(150);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();

        // 主线程：持续打印结果
        for (int i = 0; i < 10; i++) {
            Thread.sleep(200);
            System.out.println("Current Value: " + storage.get());
        }
    }
}