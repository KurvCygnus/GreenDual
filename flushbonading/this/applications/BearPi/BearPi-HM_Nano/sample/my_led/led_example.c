#include <unistd.h>
#include "ohos_init.h"
#include "wifiiot_gpio.h"
#include "wifiiot_gpio_ex.h"
#include <stdio.h>

/* void Led_Sample(void)
{
    printf("i am init now\r\n");
    GpioInit(); // 初始化GPIO
    IoSetFunc(WIFI_IOT_IO_NAME_GPIO_2, WIFI_IOT_IO_FUNC_GPIO_2_GPIO); // 设置GPIO_2的复用功能为普通GPIO
    GpioSetDir(WIFI_IOT_IO_NAME_GPIO_2, WIFI_IOT_GPIO_DIR_OUT); // 设置GPIO_2为输出模式

    for (int i = 0; i < 10; i++) {
        printf("i am in for now\r\n");
        GpioSetOutputVal(WIFI_IOT_IO_NAME_GPIO_2, 1); // 设置GPIO_2输出高电平点亮LED灯
        //usleep(2000000); // 延时2秒（2000000微秒）
        usleep(20000); // 延时
        GpioSetOutputVal(WIFI_IOT_IO_NAME_GPIO_2, 0); // 设置GPIO_2输出低电平熄灭LED灯
        //usleep(2000000); // 延时2秒（2000000微秒）
    }

    // 注意：循环结束后，LED灯将保持最后一次的状态（在这个例子中是熄灭状态）
}
 */
//开灯方法
void Led_On(void){
        GpioInit(); // 初始化GPIO
    IoSetFunc(WIFI_IOT_IO_NAME_GPIO_2, WIFI_IOT_IO_FUNC_GPIO_2_GPIO); // 设置GPIO_2的复用功能为普通GPIO
    GpioSetDir(WIFI_IOT_IO_NAME_GPIO_2, WIFI_IOT_GPIO_DIR_OUT); // 设置GPIO_2为输出模式
    GpioSetOutputVal(WIFI_IOT_IO_NAME_GPIO_2, 1); // 设置GPIO_2输出高电平点亮LED灯
}
//关灯方法
void Led_Off(void){
        GpioInit(); // 初始化GPIO
    IoSetFunc(WIFI_IOT_IO_NAME_GPIO_2, WIFI_IOT_IO_FUNC_GPIO_2_GPIO); // 设置GPIO_2的复用功能为普通GPIO
    GpioSetDir(WIFI_IOT_IO_NAME_GPIO_2, WIFI_IOT_GPIO_DIR_OUT); // 设置GPIO_2为输出模式
    GpioSetOutputVal(WIFI_IOT_IO_NAME_GPIO_2, 0); // 设置GPIO_2输出低电平熄灭LED灯
}
//注册
APP_FEATURE_INIT(Led_On);
APP_FEATURE_INIT(Led_Off);

//APP_FEATURE_INIT(Led_Sample); // 在系统启动时自动调用Led_Sample函数