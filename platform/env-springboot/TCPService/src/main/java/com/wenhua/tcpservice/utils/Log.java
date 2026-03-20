package com.wenhua.tcpservice.utils;

//日志工具类
public class Log {

    //
    public static boolean needLog = false;
    public static boolean needLog2 = true;
    public static void c(String msg){
        if(!needLog2){
            return;
        }
        System.out.println(msg);
    }

    public static void d(String msg){
        if(!needLog){
            return;
        }
        System.out.println(msg);
    }

    public static void e(String msg){
        System.err.println(msg);
    }

    public static void l(String msg){
        if(!needLog){
            return;
        }
        System.out.println(msg);
    }

    public static int count=10;
    public static void t(String msg){
        if(!needLog)
            return;
        if(count>0){
            System.out.println(msg);
        }else{
            return;
        }
        count--;
    }

    //间隔多少秒打印
    public static void t(String msg,int time){
        if(!needLog)
            return;
        if(count>0){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true){
                        try {
                            Thread.sleep(time*1000);
                            System.out.println(msg);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }).start();
        }
    }
}
