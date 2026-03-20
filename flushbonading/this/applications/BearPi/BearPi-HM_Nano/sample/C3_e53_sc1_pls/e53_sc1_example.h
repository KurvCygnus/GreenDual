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

#ifndef EXAMPLE_LIGHT_CONTROL_H
#define EXAMPLE_LIGHT_CONTROL_H

// 包含必要的头文件（注意：这些应该放在.c文件中，除非它们是公共接口的一部分）
// #include "ohos_init.h"
// #include "cmsis_os2.h"
// #include "E53_SC1.h"

// 定义一些常量或宏（如果需要）
#define ON  1
#define OFF 0

// 函数声明（公共接口）
void openLight(void);   // 打开灯
void closeLight(void);  // 关闭灯
int getLux(void);       // 获取感光器值
void startAuto(void);   // 开始自动调节亮度
void stopAuto(void);    // 停止自动调节亮度
int getAuto(void); // 获取自动调节亮度的状态
int getLastLightStatus(void);// 获取上一次的亮灯状态
// 注意：以下函数和变量是私有的，不应该在头文件中声明
// static void Example_Task(void);
// static void ExampleEntry(void);
// static int isAuto;

#endif // EXAMPLE_LIGHT_CONTROL_H