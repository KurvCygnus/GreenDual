/*
 * Copyright (c) 2020 Nanjing Xiaoxiongpai Intelligent Technology Co., Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
//全局配置
#include "../../../../../global_settings.h"

#include <stdio.h>
#include <string.h>
#include <unistd.h>

#include "ohos_init.h"
#include "cmsis_os2.h"
#include "E53_SC1.h"

#define TASK_STACK_SIZE 1024 * 8
#define TASK_PRIO 25

//上一次灯是开还是关
static int lastLightStatus = 1;
//获取上一次灯开关状态
int getLastLightStatus(void){
    return lastLightStatus;
}

//开灯方法
void openLight(void){
    //开灯
    Light_StatusSet(ON);
    lastLightStatus = 1;
}
//关灯方法
void closeLight(void){
    //关灯
    Light_StatusSet(OFF);
    lastLightStatus = 0;
}



//获取感光器值(int)方法
int getLux(void){
    return (int)E53_SC1_Read_Data();
}

//是否开启自动感应(默认开启)
static int isAuto = 1;

//开启自动感应方法
void startAuto(void){
    isAuto = 1;
}

//关闭自动感应方法
void stopAuto(void){
    isAuto = 0;
}

//获取自动感应值
int getAuto(void){
    return isAuto;
}

//剩下的方法不对外开放

static void Example_Task(void)
{
    float Lux;
    E53_SC1_Init();

    while (1)
    {
        if(isAuto){
            //需要开启自动感应才会开启下面功能
            printf("=======================================\r\n");
            printf("*************E53_SC1_example***********\r\n");
            printf("=======================================\r\n");
            Lux = E53_SC1_Read_Data();
            printf("Lux data:%.2f\r\n", Lux);
            if (Lux < AUTO_MIN_VALUE/* 这是触发值(20) */)
            {
                Light_StatusSet(ON);
            }
            else
            {
                Light_StatusSet(OFF);
            }
        }
        
        usleep(1000000);
    }
}

static void ExampleEntry(void)
{
    osThreadAttr_t attr;

    attr.name = "Example_Task";
    attr.attr_bits = 0U;
    attr.cb_mem = NULL;
    attr.cb_size = 0U;
    attr.stack_mem = NULL;
    attr.stack_size = TASK_STACK_SIZE;
    attr.priority = TASK_PRIO;

    if (osThreadNew((osThreadFunc_t)Example_Task, NULL, &attr) == NULL)
    {
        printf("Falied to create Example_Task!\n");
    }
}

APP_FEATURE_INIT(ExampleEntry);