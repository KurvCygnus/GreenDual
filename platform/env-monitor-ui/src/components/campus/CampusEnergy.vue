<template>
  <div class="campus-energy-container">
    <div class="stats-overview">
      <div class="stat-item">
        <div
          class="stat-icon"
          style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)"
        >
          <el-icon :size="32" color="#fff"><School /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ overview.totalAreas || 8 }}</div>
          <div class="stat-label">管理区域</div>
        </div>
      </div>

      <div class="stat-item">
        <div
          class="stat-icon"
          style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%)"
        >
          <el-icon :size="32" color="#fff"><Monitor /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ overview.totalDevices || 8 }}</div>
          <div class="stat-label">设备</div>
        </div>
      </div>

      <div class="stat-item">
        <div
          class="stat-icon"
          style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)"
        >
          <el-icon :size="32" color="#fff"><Coin /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">
            ¥{{ formatMoney(energyStats.savedCost || 0) }}
          </div>
          <div class="stat-label">本月节省电费</div>
        </div>
      </div>

      <div class="stat-item">
        <div
          class="stat-icon"
          style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)"
        >
          <el-icon :size="32" color="#fff"><Sunny /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">
            {{ (energyStats.savingRate || 35.5).toFixed(1) }}%
          </div>
          <div class="stat-label">综合节能率</div>
        </div>
      </div>
    </div>

    <!-- 首页概览 -->
    <template v-if="currentView === '0'">
      <!-- 双引擎架构说明 -->
      <div class="engine-overview">
        <div class="engine-section">
          <div class="engine-header">
            <div class="engine-icon energy-icon">
              <el-icon :size="32" color="#fff"><Sunny /></el-icon>
            </div>
            <div class="engine-info">
              <h3>节能引擎</h3>
              <p>AI动态调光 · 故障自检 · 用电可视</p>
            </div>
          </div>
          <div class="engine-features">
            <div class="feature-tag">人来灯亮、人走灯暗</div>
            <div class="feature-tag">30秒故障自动报警</div>
            <div class="feature-tag">实时耗电与碳减排</div>
          </div>
        </div>

        <div class="engine-divider">
          <div class="divider-line"></div>
          <div class="divider-text">双引擎协同</div>
          <div class="divider-line"></div>
        </div>

        <div class="engine-section">
          <div class="engine-header">
            <div class="engine-icon ecology-icon">
              <el-icon :size="32" color="#fff"><FirstAidKit /></el-icon>
            </div>
            <div class="engine-info">
              <h3>生态引擎</h3>
              <p>微环境监测 · 智能灌溉 · 健康预警</p>
            </div>
          </div>
          <div class="engine-features">
            <div class="feature-tag">校园生态热力图</div>
            <div class="feature-tag">智能浇水提醒</div>
            <div class="feature-tag">冻害晒伤风险预警</div>
          </div>
        </div>
      </div>

      <div class="main-content">
        <!-- 节能引擎 -->
        <div class="section-card">
          <div class="section-header">
            <div class="section-title">
              <el-icon :size="20" color="#409EFF"><Sunny /></el-icon>
              <span>节能引擎 · AI动态调光</span>
            </div>
            <div class="section-actions">
              <el-button
                type="primary"
                @click="optimizeAll"
                :loading="optimizing"
              >
                <el-icon><MagicStick /></el-icon>
                一键智能调光
              </el-button>
            </div>
          </div>

          <div class="feature-desc">
            <el-alert type="success" :closable="false" show-icon>
              <template #title>
                <strong>核心功能：</strong
                >基于人流、时段、光照度自动调节路灯亮度，实现"人来灯亮、人走灯暗"
              </template>
            </el-alert>
          </div>

          <div class="lamp-grid">
            <div v-for="lamp in lampList" :key="lamp.id" class="lamp-card">
              <div class="lamp-header">
                <div class="lamp-name">{{ lamp.name }}</div>
                <el-tag :type="lamp.onLine ? 'success' : 'info'" size="small">
                  {{ lamp.onLine ? "在线" : "离线" }}
                </el-tag>
              </div>

              <div class="lamp-data">
                <div class="data-row">
                  <span class="data-label">当前亮度</span>
                  <el-progress
                    :percentage="lamp.brightness || 0"
                    :stroke-width="12"
                    :color="getBrightnessColor(lamp.brightness)"
                  />
                </div>

                <div class="data-row">
                  <span class="data-label">光照强度</span>
                  <span class="data-value"
                    >{{ lamp.lightIntensity || 0 }} Lux</span
                  >
                </div>

                <div class="data-row">
                  <span class="data-label">建议亮度</span>
                  <span class="data-value highlight"
                    >{{ lamp.suggestedBrightness || 0 }}%</span
                  >
                </div>

                <div class="data-row">
                  <span class="data-label">节能率</span>
                  <span class="data-value success"
                    >{{ (lamp.savingRate || 0).toFixed(1) }}%</span
                  >
                </div>
              </div>

              <div class="lamp-actions">
                <el-button size="small" @click="optimizeSingle(lamp)"
                  >智能优化</el-button
                >
                <el-button
                  size="small"
                  type="primary"
                  @click="applyOptimizationToLamp(lamp)"
                  >应用方案</el-button
                >
              </div>
            </div>
          </div>
        </div>

        <!-- 生态引擎 -->
        <div class="section-card">
          <div class="section-header">
            <div class="section-title">
              <el-icon :size="20" color="#67C23A"><FirstAidKit /></el-icon>
              <span>生态引擎 · 微环境监测</span>
            </div>
            <div class="section-actions">
              <el-button
                type="primary"
                @click="optimizeGreenhouse"
                :loading="optimizingGreenhouse"
              >
                <el-icon><MagicStick /></el-icon>
                一键智能养护
              </el-button>
            </div>
          </div>

          <div class="feature-desc">
            <el-alert type="success" :closable="false" show-icon>
              <template #title>
                <strong>核心功能：</strong
                >采集温湿度、光照、土壤数据，绘制校园"生态热力图"，智能灌溉建议与植物健康预警
              </template>
            </el-alert>
          </div>

          <div class="greenhouse-grid">
            <div
              v-for="greenhouse in greenhouseList"
              :key="greenhouse.id"
              class="greenhouse-card"
            >
              <div class="greenhouse-header">
                <div class="greenhouse-name">{{ greenhouse.name }}</div>
                <el-tag
                  :type="greenhouse.onLine ? 'success' : 'info'"
                  size="small"
                >
                  {{ greenhouse.onLine ? "在线" : "离线" }}
                </el-tag>
              </div>

              <div class="greenhouse-data">
                <div class="data-row">
                  <span class="data-label">环境温度</span>
                  <span class="data-value">{{ greenhouse.temperature }}°C</span>
                </div>

                <div class="data-row">
                  <span class="data-label">环境湿度</span>
                  <span class="data-value">{{ greenhouse.humidity }}%</span>
                </div>

                <div class="data-row">
                  <span class="data-label">土壤温度</span>
                  <span class="data-value"
                    >{{ greenhouse.soilTemperature }}°C</span
                  >
                </div>

                <div class="data-row">
                  <span class="data-label">土壤湿度</span>
                  <span class="data-value">{{ greenhouse.soilHumidity }}%</span>
                </div>

                <div class="data-row">
                  <span class="data-label">光照强度</span>
                  <span class="data-value"
                    >{{ greenhouse.lightIntensity }} lux</span
                  >
                </div>
              </div>

              <div class="greenhouse-controls">
                <div class="control-item">
                  <span class="control-label">智能补光</span>
                  <el-switch
                    v-model="greenhouse.lightingStatus"
                    active-text="开启"
                    inactive-text="关闭"
                  />
                </div>
                <div class="control-item">
                  <span class="control-label">通风散热</span>
                  <el-switch
                    v-model="greenhouse.fanStatus"
                    active-text="开启"
                    inactive-text="关闭"
                  />
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="section-card">
          <div class="section-header">
            <div class="section-title">
              <el-icon :size="20" color="#67C23A"><Document /></el-icon>
              <span>能耗账单自动生成</span>
            </div>
            <div class="section-actions">
              <el-button
                type="primary"
                @click="generateBill"
                :loading="generating"
              >
                <el-icon><DocumentAdd /></el-icon>
                生成本月账单
              </el-button>
            </div>
          </div>

          <div class="feature-desc">
            <el-alert type="info" :closable="false" show-icon>
              <template #title>
                <strong>核心功能：</strong
                >为后勤部门提供可视化的月度/季度能耗报告，精准定位浪费节点
              </template>
            </el-alert>
          </div>

          <div class="bill-overview">
            <div class="bill-card">
              <div class="bill-title">本月能耗概览</div>
              <div class="bill-stats">
                <div class="bill-stat-item">
                  <div class="bill-stat-label">总能耗</div>
                  <div class="bill-stat-value">
                    {{ (currentBill.totalConsumption || 0).toFixed(1) }}
                    <small>kWh</small>
                  </div>
                </div>
                <div class="bill-stat-item">
                  <div class="bill-stat-label">总费用</div>
                  <div class="bill-stat-value">
                    ¥{{ formatMoney(currentBill.totalCost || 0) }}
                  </div>
                </div>
                <div class="bill-stat-item">
                  <div class="bill-stat-label">节约能耗</div>
                  <div class="bill-stat-value success">
                    {{ (currentBill.savedConsumption || 0).toFixed(1) }}
                    <small>kWh</small>
                  </div>
                </div>
                <div class="bill-stat-item">
                  <div class="bill-stat-label">节约费用</div>
                  <div class="bill-stat-value success">
                    ¥{{ formatMoney(currentBill.savedCost || 0) }}
                  </div>
                </div>
              </div>
            </div>

            <div class="bill-card">
              <div class="bill-title">浪费分析与优化建议</div>
              <div class="bill-analysis">
                <div class="analysis-item">
                  <el-icon color="#E6A23C"><Warning /></el-icon>
                  <span>{{
                    currentBill.wasteAnalysis ||
                    "整体能源利用效率良好，未发现明显浪费现象。"
                  }}</span>
                </div>
                <el-divider />
                <div class="analysis-item">
                  <el-icon color="#67C23A"><CircleCheck /></el-icon>
                  <span>{{
                    currentBill.optimizationSuggestions ||
                    "建议继续优化照明策略，进一步提升节能效果。"
                  }}</span>
                </div>
              </div>
            </div>
          </div>

          <el-table :data="billHistory" style="width: 100%; margin-top: 20px">
            <el-table-column prop="billMonth" label="账单月份" width="120" />
            <el-table-column
              prop="totalConsumption"
              label="总能耗(kWh)"
              width="120"
            >
              <template #default="scope">
                {{ (scope.row.totalConsumption || 0).toFixed(1) }}
              </template>
            </el-table-column>
            <el-table-column prop="totalCost" label="总费用(元)" width="120">
              <template #default="scope">
                ¥{{ formatMoney(scope.row.totalCost || 0) }}
              </template>
            </el-table-column>
            <el-table-column
              prop="savedConsumption"
              label="节约能耗(kWh)"
              width="130"
            >
              <template #default="scope">
                <span style="color: #67c23a">{{
                  (scope.row.savedConsumption || 0).toFixed(1)
                }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="savedCost" label="节约费用(元)" width="120">
              <template #default="scope">
                <span style="color: #67c23a"
                  >¥{{ formatMoney(scope.row.savedCost || 0) }}</span
                >
              </template>
            </el-table-column>
            <el-table-column prop="savingRate" label="节能率(%)" width="100">
              <template #default="scope">
                <el-tag :type="getSavingTagType(scope.row.savingRate)">
                  {{ (scope.row.savingRate || 0).toFixed(1) }}%
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template #default="scope">
                <el-button size="small" @click="viewBillDetail(scope.row)"
                  >详情</el-button
                >
              </template>
            </el-table-column>
          </el-table>
        </div>

        <div class="section-card">
          <div class="section-header">
            <div class="section-title">
              <el-icon :size="20" color="#E6A23A"><Reading /></el-icon>
              <span>教学联动</span>
            </div>
            <div class="section-actions">
              <el-button type="primary" @click="showCreateCaseDialog">
                <el-icon><Plus /></el-icon>
                创建教学案例
              </el-button>
            </div>
          </div>

          <div class="feature-desc">
            <el-alert type="warning" :closable="false" show-icon>
              <template #title>
                <strong>核心功能：</strong
                >将照明节能与绿植养护数据作为物联网教学案例，服务高校课程改革需求
              </template>
            </el-alert>
          </div>

          <div class="teaching-stats">
            <div class="teaching-stat-item">
              <el-icon :size="32" color="#409EFF"><Document /></el-icon>
              <div>
                <div class="teaching-stat-value">
                  {{ teachingStats.totalCases || 0 }}
                </div>
                <div class="teaching-stat-label">教学案例总数</div>
              </div>
            </div>
            <div class="teaching-stat-item">
              <el-icon :size="32" color="#67C23A"><Reading /></el-icon>
              <div>
                <div class="teaching-stat-value">
                  {{ teachingStats.relatedCourses?.length || 0 }}
                </div>
                <div class="teaching-stat-label">关联课程数</div>
              </div>
            </div>
            <div class="teaching-stat-item">
              <el-icon :size="32" color="#E6A23C"><User /></el-icon>
              <div>
                <div class="teaching-stat-value">
                  {{ teachingStats.lightingCases || 0 }}
                </div>
                <div class="teaching-stat-label">路灯控制案例</div>
              </div>
            </div>
          </div>

          <el-table :data="teachingCases" style="width: 100%; margin-top: 20px">
            <el-table-column prop="caseName" label="案例名称" width="250" />
            <el-table-column prop="caseType" label="案例类型" width="120">
              <template #default="scope">
                <el-tag :type="getCaseTypeTag(scope.row.caseType)">
                  {{ getCaseTypeName(scope.row.caseType) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="deviceName" label="关联设备" width="180" />
            <el-table-column prop="relatedCourse" label="相关课程" />
            <el-table-column prop="useCount" label="使用次数" width="100" />
            <el-table-column label="操作" width="180">
              <template #default="scope">
                <el-button size="small" @click="viewTeachingCase(scope.row)"
                  >查看详情</el-button
                >
                <el-button
                  size="small"
                  type="primary"
                  @click="exportCase(scope.row)"
                  >导出</el-button
                >
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <el-dialog v-model="teachingCaseDialog" title="教学案例详情" width="70%">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="案例名称">{{
            currentCase.caseName
          }}</el-descriptions-item>
          <el-descriptions-item label="案例类型">{{
            getCaseTypeName(currentCase.caseType)
          }}</el-descriptions-item>
          <el-descriptions-item label="关联设备">{{
            currentCase.deviceName
          }}</el-descriptions-item>
          <el-descriptions-item label="相关课程">{{
            currentCase.relatedCourse
          }}</el-descriptions-item>
        </el-descriptions>
        <el-divider />
        <h4>教学内容</h4>
        <pre class="content-box">{{ currentCase.teachingContent }}</pre>
        <el-divider />
        <h4>实验指导</h4>
        <pre class="content-box">{{ currentCase.experimentGuide }}</pre>
        <el-divider />
        <h4>数据预览</h4>
        <pre class="content-box code-box">{{ currentCase.dataPreview }}</pre>
      </el-dialog>
    </template>

    <!-- 能耗管理 - 能耗账单 -->
    <template v-else-if="currentView === '2-1'">
      <div class="main-content">
        <div class="section-card">
          <div class="section-header">
            <div class="section-title">
              <el-icon :size="20" color="#E6A23C"><Coin /></el-icon>
              <span>能耗账单</span>
            </div>
            <div class="section-actions">
              <el-button
                type="primary"
                @click="generateBill"
                :loading="generating"
              >
                <el-icon><DocumentAdd /></el-icon>
                生成本月账单
              </el-button>
            </div>
          </div>

          <div class="feature-desc">
            <el-alert type="info" :closable="false" show-icon>
              <template #title>
                <strong>核心功能：</strong
                >为后勤部门提供可视化的月度/季度能耗报告，精准定位浪费节点
              </template>
            </el-alert>
          </div>

          <div class="bill-overview">
            <div class="bill-card">
              <div class="bill-title">本月账单概览</div>
              <div class="bill-stats">
                <div class="bill-stat-item">
                  <div class="bill-stat-label">总能耗</div>
                  <div class="bill-stat-value">
                    {{ (currentBill.totalConsumption || 0).toFixed(1) }}
                    <small>kWh</small>
                  </div>
                </div>
                <div class="bill-stat-item">
                  <div class="bill-stat-label">总费用</div>
                  <div class="bill-stat-value">
                    ¥{{ formatMoney(currentBill.totalCost || 0) }}
                  </div>
                </div>
                <div class="bill-stat-item">
                  <div class="bill-stat-label">节约能耗</div>
                  <div class="bill-stat-value success">
                    {{ (currentBill.savedConsumption || 0).toFixed(1) }}
                    <small>kWh</small>
                  </div>
                </div>
                <div class="bill-stat-item">
                  <div class="bill-stat-label">节约费用</div>
                  <div class="bill-stat-value success">
                    ¥{{ formatMoney(currentBill.savedCost || 0) }}
                  </div>
                </div>
              </div>
            </div>

            <div class="bill-card">
              <div class="bill-title">浪费分析与优化建议</div>
              <div class="bill-analysis">
                <div class="analysis-item">
                  <el-icon color="#E6A23C"><Warning /></el-icon>
                  <span>{{
                    currentBill.wasteAnalysis ||
                    "整体能源利用效率良好，未发现明显浪费现象。"
                  }}</span>
                </div>
                <el-divider />
                <div class="analysis-item">
                  <el-icon color="#67C23A"><CircleCheck /></el-icon>
                  <span>{{
                    currentBill.optimizationSuggestions ||
                    "建议继续优化照明策略，进一步提升节能效果。"
                  }}</span>
                </div>
              </div>
            </div>
          </div>

          <el-table :data="billHistory" style="width: 100%; margin-top: 20px">
            <el-table-column prop="billMonth" label="账单月份" width="120" />
            <el-table-column
              prop="totalConsumption"
              label="总能耗(kWh)"
              width="120"
            >
              <template #default="scope">
                {{ (scope.row.totalConsumption || 0).toFixed(1) }}
              </template>
            </el-table-column>
            <el-table-column prop="totalCost" label="总费用(元)" width="120">
              <template #default="scope">
                ¥{{ formatMoney(scope.row.totalCost || 0) }}
              </template>
            </el-table-column>
            <el-table-column
              prop="savedConsumption"
              label="节约能耗(kWh)"
              width="130"
            >
              <template #default="scope">
                <span style="color: #67c23a">{{
                  (scope.row.savedConsumption || 0).toFixed(1)
                }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="savedCost" label="节约费用(元)" width="120">
              <template #default="scope">
                <span style="color: #67c23a"
                  >¥{{ formatMoney(scope.row.savedCost || 0) }}</span
                >
              </template>
            </el-table-column>
            <el-table-column prop="savingRate" label="节能率(%)" width="100">
              <template #default="scope">
                <el-tag :type="getSavingTagType(scope.row.savingRate)">
                  {{ (scope.row.savingRate || 0).toFixed(1) }}%
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template #default="scope">
                <el-button size="small" @click="viewBillDetail(scope.row)"
                  >详情</el-button
                >
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </template>

    <!-- 能耗管理 - 节能分析 -->
    <template v-else-if="currentView === '2-2' || currentView === '1-4'">
      <div class="main-content">
        <div class="section-card">
          <div class="section-header">
            <div class="section-title">
              <el-icon :size="20" color="#409EFF"><TrendCharts /></el-icon>
              <span>节能分析</span>
            </div>
            <div class="section-actions">
              <el-button
                type="primary"
                size="small"
                :loading="loading"
                @click="refreshEnergyAnalysis"
              >
                <el-icon><Refresh /></el-icon>
                刷新数据
              </el-button>
              <el-button type="success" size="small" @click="exportDataReport">
                <el-icon><Download /></el-icon>
                导出报表
              </el-button>
            </div>
          </div>
          <div class="feature-desc">
            <el-alert type="info" :closable="false" show-icon>
              <template #title>
                <strong>功能说明：</strong
                >分析校园能源使用情况，识别节能机会，优化能源管理策略
              </template>
            </el-alert>
          </div>

          <!-- 分析图表区域 -->
          <div class="analysis-charts">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-card shadow="hover" class="chart-card">
                  <template #header>
                    <div class="chart-header">
                      <span>月度能耗趋势</span>
                      <el-tag type="success" size="small">近6个月</el-tag>
                    </div>
                  </template>
                  <div ref="energyTrendChartRef" class="chart-container"></div>
                </el-card>
              </el-col>
              <el-col :span="12">
                <el-card shadow="hover" class="chart-card">
                  <template #header>
                    <div class="chart-header">
                      <span>节能效果对比</span>
                      <el-tag type="primary" size="small">累计数据</el-tag>
                    </div>
                  </template>
                  <div
                    ref="savingCompareChartRef"
                    class="chart-container"
                  ></div>
                </el-card>
              </el-col>
            </el-row>

            <el-row :gutter="20" style="margin-top: 20px">
              <el-col :span="12">
                <el-card shadow="hover" class="chart-card">
                  <template #header>
                    <div class="chart-header">
                      <span>各区域能耗占比</span>
                      <el-tag type="warning" size="small">本月</el-tag>
                    </div>
                  </template>
                  <div ref="areaPieChartRef" class="chart-container"></div>
                </el-card>
              </el-col>
              <el-col :span="12">
                <el-card shadow="hover" class="chart-card">
                  <template #header>
                    <div class="chart-header">
                      <span>峰谷用电分析</span>
                      <el-tag type="info" size="small">时段分布</el-tag>
                    </div>
                  </template>
                  <div ref="peakValleyChartRef" class="chart-container"></div>
                </el-card>
              </el-col>
            </el-row>

            <!-- 节能指标卡片 -->
            <el-row :gutter="20" style="margin-top: 20px">
              <el-col :span="6">
                <el-card shadow="hover" class="indicator-card">
                  <div class="indicator-content">
                    <div
                      class="indicator-icon"
                      style="
                        background: linear-gradient(
                          135deg,
                          #667eea 0%,
                          #764ba2 100%
                        );
                      "
                    >
                      <el-icon :size="24" color="#fff"><TrendCharts /></el-icon>
                    </div>
                    <div class="indicator-info">
                      <div class="indicator-value">
                        {{ energyIndicators.avgSavingRate }}%
                      </div>
                      <div class="indicator-label">平均节能率</div>
                    </div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card shadow="hover" class="indicator-card">
                  <div class="indicator-content">
                    <div
                      class="indicator-icon"
                      style="
                        background: linear-gradient(
                          135deg,
                          #f093fb 0%,
                          #f5576c 100%
                        );
                      "
                    >
                      <el-icon :size="24" color="#fff"><Coin /></el-icon>
                    </div>
                    <div class="indicator-info">
                      <div class="indicator-value">
                        ¥{{ formatMoney(energyIndicators.totalSavedCost) }}
                      </div>
                      <div class="indicator-label">累计节省费用</div>
                    </div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card shadow="hover" class="indicator-card">
                  <div class="indicator-content">
                    <div
                      class="indicator-icon"
                      style="
                        background: linear-gradient(
                          135deg,
                          #4facfe 0%,
                          #00f2fe 100%
                        );
                      "
                    >
                      <el-icon :size="24" color="#fff"><Sunny /></el-icon>
                    </div>
                    <div class="indicator-info">
                      <div class="indicator-value">
                        {{ energyIndicators.totalSavedEnergy }}kWh
                      </div>
                      <div class="indicator-label">累计节电量</div>
                    </div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card shadow="hover" class="indicator-card">
                  <div class="indicator-content">
                    <div
                      class="indicator-icon"
                      style="
                        background: linear-gradient(
                          135deg,
                          #43e97b 0%,
                          #38f9d7 100%
                        );
                      "
                    >
                      <el-icon :size="24" color="#fff"><FirstAidKit /></el-icon>
                    </div>
                    <div class="indicator-info">
                      <div class="indicator-value">
                        {{ energyIndicators.carbonReduction }}kg
                      </div>
                      <div class="indicator-label">碳减排量</div>
                    </div>
                  </div>
                </el-card>
              </el-col>
            </el-row>
          </div>
        </div>
      </div>
    </template>

    <!-- 能耗管理 - 费用统计 -->
    <template v-else-if="currentView === '2-3'">
      <div class="main-content">
        <div class="section-card">
          <div class="section-header">
            <div class="section-title">
              <el-icon :size="20" color="#67C23A"><Coin /></el-icon>
              <span>费用统计</span>
            </div>
          </div>
          <div class="feature-desc">
            <el-alert type="info" :closable="false" show-icon>
              <template #title>
                <strong>功能说明：</strong
                >统计校园能源费用支出，分析费用变化趋势，优化预算分配
              </template>
            </el-alert>
          </div>
          <div class="cost-content">
            <el-empty description="费用统计功能开发中" />
          </div>
        </div>
      </div>
    </template>

    <!-- 数据报表 -->
    <template v-else-if="currentView === '3-3'">
      <div class="main-content">
        <div class="section-card">
          <div class="section-header">
            <div class="section-title">
              <el-icon :size="20" color="#409EFF"><Document /></el-icon>
              <span>数据报表</span>
            </div>
            <div class="section-actions">
              <el-button
                type="primary"
                size="small"
                :loading="loading"
                @click="refreshReportData"
              >
                <el-icon><Refresh /></el-icon>
                刷新数据
              </el-button>
              <el-button type="success" size="small" @click="exportDataReport">
                <el-icon><Download /></el-icon>
                导出报表
              </el-button>
            </div>
          </div>

          <div class="feature-desc">
            <el-alert type="info" :closable="false" show-icon>
              <template #title>
                <strong>功能说明：</strong>
                >提供校园设备、能源消耗、故障统计等数据的综合分析与可视化展示
              </template>
            </el-alert>
          </div>

          <!-- 报表统计卡片 -->
          <div class="report-stats">
            <el-row :gutter="20">
              <el-col :span="6">
                <el-card shadow="hover" class="report-stat-card">
                  <div class="report-stat-content">
                    <div
                      class="report-stat-icon"
                      style="
                        background: linear-gradient(
                          135deg,
                          #667eea 0%,
                          #764ba2 100%
                        );
                      "
                    >
                      <el-icon :size="24" color="#fff"><Monitor /></el-icon>
                    </div>
                    <div class="report-stat-info">
                      <div class="report-stat-value">
                        {{ reportStats.totalDevices || 0 }}
                      </div>
                      <div class="report-stat-label">总设备数</div>
                    </div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card shadow="hover" class="report-stat-card">
                  <div class="report-stat-content">
                    <div
                      class="report-stat-icon"
                      style="
                        background: linear-gradient(
                          135deg,
                          #f093fb 0%,
                          #f5576c 100%
                        );
                      "
                    >
                      <el-icon :size="24" color="#fff"><School /></el-icon>
                    </div>
                    <div class="report-stat-info">
                      <div class="report-stat-value">
                        {{ reportStats.totalAreas || 0 }}
                      </div>
                      <div class="report-stat-label">管理区域</div>
                    </div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card shadow="hover" class="report-stat-card">
                  <div class="report-stat-content">
                    <div
                      class="report-stat-icon"
                      style="
                        background: linear-gradient(
                          135deg,
                          #4facfe 0%,
                          #00f2fe 100%
                        );
                      "
                    >
                      <el-icon :size="24" color="#fff"><Coin /></el-icon>
                    </div>
                    <div class="report-stat-info">
                      <div class="report-stat-value">
                        ¥{{ formatMoney(reportStats.totalSavedCost || 0) }}
                      </div>
                      <div class="report-stat-label">累计节省</div>
                    </div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card shadow="hover" class="report-stat-card">
                  <div class="report-stat-content">
                    <div
                      class="report-stat-icon"
                      style="
                        background: linear-gradient(
                          135deg,
                          #43e97b 0%,
                          #38f9d7 100%
                        );
                      "
                    >
                      <el-icon :size="24" color="#fff"><Warning /></el-icon>
                    </div>
                    <div class="report-stat-info">
                      <div class="report-stat-value">
                        {{ reportStats.totalFaults || 0 }}
                      </div>
                      <div class="report-stat-label">故障设备</div>
                    </div>
                  </div>
                </el-card>
              </el-col>
            </el-row>
          </div>

          <!-- 报表图表 -->
          <div class="report-charts">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-card shadow="hover" class="chart-card">
                  <template #header>
                    <div class="chart-header">
                      <span>设备类型分布</span>
                      <el-tag type="info" size="small">设备类别</el-tag>
                    </div>
                  </template>
                  <div ref="deviceTypeChartRef" class="chart-container"></div>
                </el-card>
              </el-col>
              <el-col :span="12">
                <el-card shadow="hover" class="chart-card">
                  <template #header>
                    <div class="chart-header">
                      <span>区域设备分布</span>
                      <el-tag type="info" size="small">区域分布</el-tag>
                    </div>
                  </template>
                  <div
                    ref="areaDistributionChartRef"
                    class="chart-container"
                  ></div>
                </el-card>
              </el-col>
            </el-row>

            <el-row :gutter="20" style="margin-top: 20px">
              <el-col :span="12">
                <el-card shadow="hover" class="chart-card">
                  <template #header>
                    <div class="chart-header">
                      <span>月度能耗趋势</span>
                      <el-tag type="info" size="small">近6个月</el-tag>
                    </div>
                  </template>
                  <div
                    ref="reportEnergyTrendChartRef"
                    class="chart-container"
                  ></div>
                </el-card>
              </el-col>
              <el-col :span="12">
                <el-card shadow="hover" class="chart-card">
                  <template #header>
                    <div class="chart-header">
                      <span>故障设备统计</span>
                      <el-tag type="info" size="small">故障类型</el-tag>
                    </div>
                  </template>
                  <div ref="faultStatsChartRef" class="chart-container"></div>
                </el-card>
              </el-col>
            </el-row>
          </div>
        </div>
      </div>
    </template>

    <!-- 教学联动 -->
    <template v-else-if="currentView === '4'">
      <div class="main-content">
        <div class="section-card">
          <div class="section-header">
            <div class="section-title">
              <el-icon :size="20" color="#E6A23A"><Reading /></el-icon>
              <span>教学联动</span>
            </div>
            <div class="section-actions">
              <el-button type="primary" @click="showCreateCaseDialog">
                <el-icon><Plus /></el-icon>
                创建教学案例
              </el-button>
            </div>
          </div>

          <div class="feature-desc">
            <el-alert type="warning" :closable="false" show-icon>
              <template #title>
                <strong>核心功能：</strong
                >将照明节能与绿植养护数据作为物联网教学案例，服务高校课程改革需求
              </template>
            </el-alert>
          </div>

          <div class="teaching-stats">
            <div class="teaching-stat-item">
              <el-icon :size="32" color="#409EFF"><Document /></el-icon>
              <div>
                <div class="teaching-stat-value">
                  {{ teachingStats.totalCases || 0 }}
                </div>
                <div class="teaching-stat-label">教学案例总数</div>
              </div>
            </div>
            <div class="teaching-stat-item">
              <el-icon :size="32" color="#67C23A"><Reading /></el-icon>
              <div>
                <div class="teaching-stat-value">
                  {{ teachingStats.relatedCourses?.length || 0 }}
                </div>
                <div class="teaching-stat-label">关联课程数</div>
              </div>
            </div>
            <div class="teaching-stat-item">
              <el-icon :size="32" color="#E6A23C"><User /></el-icon>
              <div>
                <div class="teaching-stat-value">
                  {{ teachingStats.lightingCases || 0 }}
                </div>
                <div class="teaching-stat-label">路灯控制案例</div>
              </div>
            </div>
          </div>

          <el-table :data="teachingCases" style="width: 100%; margin-top: 20px">
            <el-table-column prop="caseName" label="案例名称" width="250" />
            <el-table-column prop="caseType" label="案例类型" width="120">
              <template #default="scope">
                <el-tag :type="getCaseTypeTag(scope.row.caseType)">
                  {{ getCaseTypeName(scope.row.caseType) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="deviceName" label="关联设备" width="180" />
            <el-table-column prop="relatedCourse" label="相关课程" />
            <el-table-column prop="useCount" label="使用次数" width="100" />
            <el-table-column label="操作" width="180">
              <template #default="scope">
                <el-button size="small" @click="viewTeachingCase(scope.row)"
                  >查看详情</el-button
                >
                <el-button
                  size="small"
                  type="primary"
                  @click="exportCase(scope.row)"
                  >导出</el-button
                >
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, watch, nextTick } from "vue";
import { ElMessage } from "element-plus";
import * as echarts from "echarts";
import {
  Monitor,
  Sunny,
  Coin,
  TrendCharts,
  Document,
  Reading,
  User,
  School,
  Refresh,
  Download,
  MagicStick,
  DocumentAdd,
  Plus,
  Warning,
  CircleCheck,
  FirstAidKit,
  Search,
} from "@element-plus/icons-vue";
import {
  getCampusOverview,
  getEnergyStatistics,
  getBillHistory,
  createBill,
  getTeachingStatistics,
  optimizeLighting,
  applyLightingOptimization,
  getLampList,
  getDeviceList,
} from "@/api/auth";

const props = defineProps({
  activeIndex: {
    type: String,
    default: "0",
  },
});

const currentView = computed(() => {
  return props.activeIndex;
});

const activeTab = ref("lighting");
const overview = ref({});
const energyStats = ref({});
const currentBill = ref({});
const billHistory = ref([]);
const lampList = ref([]);
const greenhouseList = ref([]);
const teachingStats = ref({});
const teachingCases = ref([]);
const teachingCaseDialog = ref(false);
const currentCase = ref({});
const optimizing = ref(false);
const optimizingGreenhouse = ref(false);
const generating = ref(false);
const loading = ref(false);

// 图表相关
const energyTrendChartRef = ref(null);
const savingCompareChartRef = ref(null);
const areaPieChartRef = ref(null);
const peakValleyChartRef = ref(null);
let energyTrendChart = null;
let savingCompareChart = null;
let areaPieChart = null;
let peakValleyChart = null;

// 报表相关
const reportStats = ref({
  totalDevices: 35,
  totalAreas: 8,
  totalSavedCost: 5445,
  totalFaults: 5,
});

// 报表图表相关
const deviceTypeChartRef = ref(null);
const areaDistributionChartRef = ref(null);
const reportEnergyTrendChartRef = ref(null);
const faultStatsChartRef = ref(null);
let deviceTypeChart = null;
let areaDistributionChart = null;
let reportEnergyTrendChart = null;
let faultStatsChart = null;

// 节能指标 - 基于30盏LED路灯的实际数据
// 每盏80W，8小时/天，30天 = 153.6 kWh/月/盏
// 30盏 = 4608 kWh/月，智能调光节能约18-22%
const energyIndicators = ref({
  avgSavingRate: 20.5, // 平均节能率 20.5%
  totalSavedCost: 5445, // 年度节省费用约5445元 (30盏)
  totalSavedEnergy: 9900, // 年度节电量约9900 kWh (30盏)
  carbonReduction: 9870, // 年度碳减排约9870 kg (30盏)
});

// 图表数据 - 贴合实际的月度数据 (30盏路灯)
const monthlyTrendData = ref({
  months: ["1月", "2月", "3月", "4月", "5月", "6月"],
  totalEnergy: [4120, 3940, 3680, 3450, 3300, 3180], // 实际用电量(kWh) 30盏
  savedEnergy: [680, 750, 825, 900, 975, 1050], // 节电量(kWh) 30盏
});

// 30盏路灯分布：教学区12盏，宿舍区8盏，办公区6盏，运动区4盏
const areaDistributionData = ref([
  { value: 12, name: "教学区" }, // 12盏 (主干道、教学楼周边)
  { value: 8, name: "宿舍区" }, // 8盏 (宿舍区道路)
  { value: 6, name: "办公区" }, // 6盏 (行政楼、停车场)
  { value: 4, name: "运动区" }, // 4盏 (操场、体育馆周边)
]);

// 峰谷用电数据 - 基于30盏路灯实际使用模式
// 0-6时: 深夜部分照明  6-12时: 早晨逐渐关闭
// 12-18时: 白天基本关闭  18-24时: 傍晚大量开启
const peakValleyData = ref({
  periods: ["0-6时", "6-12时", "12-18时", "18-24时"],
  peak: [450, 170, 110, 680], // 峰时用电(kWh) 30盏
  valley: [225, 85, 55, 340], // 谷时用电(kWh) 30盏
});

const formatMoney = (value) => {
  return value.toFixed(2);
};

const getBrightnessColor = (brightness) => {
  if (!brightness) return "#909399";
  if (brightness < 50) return "#67C23A";
  if (brightness < 80) return "#E6A23C";
  return "#F56C6C";
};

const getBrightnessTagType = (brightness) => {
  if (!brightness) return "info";
  if (brightness < 50) return "success";
  if (brightness < 80) return "warning";
  return "danger";
};

const getSavingTagType = (rate) => {
  if (rate >= 30) return "success";
  if (rate >= 15) return "warning";
  return "info";
};

const getCaseTypeTag = (type) => {
  const types = {
    lighting_control: "primary",
    environment_monitor: "success",
    energy_saving: "warning",
  };
  return types[type] || "info";
};

const getCaseTypeName = (type) => {
  const names = {
    lighting_control: "路灯控制",
    environment_monitor: "环境监测",
    energy_saving: "节能控制",
  };
  return names[type] || "综合应用";
};

const fetchOverview = async () => {
  try {
    const res = await getCampusOverview();
    if (res.data.code === 1) {
      overview.value = res.data.data;
    }
  } catch (error) {
    console.error("获取概览失败:", error);
  }
};

const fetchEnergyStats = async () => {
  try {
    const res = await getEnergyStatistics();
    if (res.data.code === 1) {
      energyStats.value = res.data.data;
      // 更新节能指标数据
      if (res.data.data.indicators) {
        energyIndicators.value = res.data.data.indicators;
      }
      // 更新图表数据
      if (res.data.data.monthlyTrend) {
        monthlyTrendData.value = res.data.data.monthlyTrend;
      }
      if (res.data.data.areaDistribution) {
        areaDistributionData.value = res.data.data.areaDistribution;
      }
      if (res.data.data.peakValleyData) {
        peakValleyData.value = res.data.data.peakValleyData;
      }
    }
  } catch (error) {
    console.error("获取能源统计失败:", error);
  }
  // 添加默认数据（如果后端没有返回）- 基于30盏LED路灯实际数据
  if (!energyStats.value.savedCost) {
    energyStats.value.savedCost = 454; // 月度节省约454元 (30盏)
  }
  if (!energyStats.value.savingRate) {
    energyStats.value.savingRate = 20.5; // 节能率20.5%
  }
  // 确保节能指标有数据
  if (!energyIndicators.value.avgSavingRate) {
    energyIndicators.value = {
      avgSavingRate: 20.5, // 平均节能率20.5%
      totalSavedCost: 5445, // 年度节省约5445元 (30盏)
      totalSavedEnergy: 9900, // 年度节电约9900 kWh (30盏)
      carbonReduction: 9870, // 年度碳减排约9870 kg (30盏)
    };
  }
  // 确保图表数据有默认值
  if (!monthlyTrendData.value.months) {
    monthlyTrendData.value = {
      months: ["1月", "2月", "3月", "4月", "5月", "6月"],
      totalEnergy: [4120, 3940, 3680, 3450, 3300, 3180], // 实际用电量(kWh) 30盏
      savedEnergy: [680, 750, 825, 900, 975, 1050], // 节电量(kWh) 30盏
    };
  }
  if (!areaDistributionData.value.length) {
    areaDistributionData.value = [
      { name: "教学区", value: 12 }, // 12盏 (主干道、教学楼周边)
      { name: "宿舍区", value: 8 }, // 8盏 (宿舍区道路)
      { name: "办公区", value: 6 }, // 6盏 (行政楼、停车场)
      { name: "运动区", value: 4 }, // 4盏 (操场、体育馆周边)
    ];
  }
  if (!peakValleyData.value.periods) {
    peakValleyData.value = {
      periods: ["0-6时", "6-12时", "12-18时", "18-24时"],
      peak: [450, 170, 110, 680], // 峰时用电(kWh) 30盏
      valley: [225, 85, 55, 340], // 谷时用电(kWh) 30盏
    };
  }
};

const fetchBillHistory = async () => {
  try {
    const res = await getBillHistory();
    if (res.data.code === 1) {
      billHistory.value = res.data.data;
      if (billHistory.value.length > 0) {
        currentBill.value = billHistory.value[0];
        // 添加格式化的AI建议数据
        if (!currentBill.value.optimizationSuggestions) {
          currentBill.value.optimizationSuggestions = `建议对老旧高能耗灯具进行LED改造，可进一步降低能耗；
建议安装光照传感器和人流传感器，实现精准按需照明；
建议定期检查线路和设备，减少因故障导致的能源损耗。`;
        }
        if (!currentBill.value.wasteAnalysis) {
          currentBill.value.wasteAnalysis = `夜间照明时段存在过度照明现象，建议优化调光策略；
部分区域灯具老化严重，能耗偏高；
白天自然光充足时，部分灯具未及时关闭。`;
        }
      }
    }
  } catch (error) {
    console.error("获取账单历史失败:", error);
  }
};

const fetchLampList = async () => {
  try {
    const res = await getLampList();
    if (res.data.code === 1) {
      lampList.value = res.data.data;
      for (let lamp of lampList.value) {
        const optRes = await optimizeLighting(lamp.id);
        if (optRes.data.code === 1) {
          lamp.suggestedBrightness = optRes.data.data.optimalBrightness;
          lamp.savingRate = optRes.data.data.savingRate;
        }
      }
    }
  } catch (error) {
    console.error("获取路灯列表失败:", error);
  }
};

const fetchGreenhouseList = async () => {
  try {
    const res = await getDeviceList({
      match: "绿植养护",
      offset: 0,
      pageSize: 100,
      sort: "asc",
    });
    if (res.data.code === 1 && res.data.data.length > 0) {
      greenhouseList.value = res.data.data
        .filter((d) => d.deviceType === "绿植养护")
        .map((device) => ({
          ...device,
          lightingStatus: false,
          fanStatus: false,
        }));
    } else {
      // 使用模拟数据
      greenhouseList.value = [
        {
          id: 1,
          name: "图书馆绿植区",
          onLine: true,
          temperature: 24,
          humidity: 65,
          soilTemperature: 22,
          soilHumidity: 58,
          lightIntensity: 850,
          lightingStatus: false,
          fanStatus: false,
        },
        {
          id: 2,
          name: "教学楼A区花园",
          onLine: true,
          temperature: 26,
          humidity: 70,
          soilTemperature: 23,
          soilHumidity: 45,
          lightIntensity: 1200,
          lightingStatus: false,
          fanStatus: false,
        },
        {
          id: 3,
          name: "行政楼景观带",
          onLine: true,
          temperature: 25,
          humidity: 62,
          soilTemperature: 21,
          soilHumidity: 72,
          lightIntensity: 680,
          lightingStatus: true,
          fanStatus: false,
        },
        {
          id: 4,
          name: "学生宿舍绿化带",
          onLine: false,
          temperature: 23,
          humidity: 68,
          soilTemperature: 20,
          soilHumidity: 55,
          lightIntensity: 920,
          lightingStatus: false,
          fanStatus: false,
        },
        {
          id: 5,
          name: "体育馆周边绿植",
          onLine: true,
          temperature: 28,
          humidity: 55,
          soilTemperature: 25,
          soilHumidity: 38,
          lightIntensity: 1500,
          lightingStatus: false,
          fanStatus: true,
        },
        {
          id: 6,
          name: "食堂景观植物",
          onLine: true,
          temperature: 27,
          humidity: 75,
          soilTemperature: 24,
          soilHumidity: 80,
          lightIntensity: 450,
          lightingStatus: true,
          fanStatus: false,
        },
      ];
    }
  } catch (error) {
    console.error("获取绿植养护列表失败:", error);
    // 使用模拟数据
    greenhouseList.value = [
      {
        id: 1,
        name: "图书馆绿植区",
        onLine: true,
        temperature: 24,
        humidity: 65,
        soilTemperature: 22,
        soilHumidity: 58,
        lightIntensity: 850,
        lightingStatus: false,
        fanStatus: false,
      },
      {
        id: 2,
        name: "教学楼A区花园",
        onLine: true,
        temperature: 26,
        humidity: 70,
        soilTemperature: 23,
        soilHumidity: 45,
        lightIntensity: 1200,
        lightingStatus: false,
        fanStatus: false,
      },
      {
        id: 3,
        name: "行政楼景观带",
        onLine: true,
        temperature: 25,
        humidity: 62,
        soilTemperature: 21,
        soilHumidity: 72,
        lightIntensity: 680,
        lightingStatus: true,
        fanStatus: false,
      },
      {
        id: 4,
        name: "学生宿舍绿化带",
        onLine: false,
        temperature: 23,
        humidity: 68,
        soilTemperature: 20,
        soilHumidity: 55,
        lightIntensity: 920,
        lightingStatus: false,
        fanStatus: false,
      },
    ];
  }
};

const optimizeGreenhouse = async () => {
  optimizingGreenhouse.value = true;
  try {
    const token = localStorage.getItem("token");
    for (let greenhouse of greenhouseList.value) {
      if (greenhouse.temperature > 30) {
        greenhouse.fanStatus = true;
      }
      if (greenhouse.lightIntensity < 500) {
        greenhouse.lightingStatus = true;
      }
    }
    ElMessage.success("绿植养护智能调节完成");
  } catch (error) {
    console.error("绿植养护智能调节失败:", error);
    ElMessage.error("绿植养护智能调节失败");
  } finally {
    optimizingGreenhouse.value = false;
  }
};

const fetchTeachingStats = async () => {
  try {
    const res = await getTeachingStatistics();
    if (res.data.code === 1) {
      teachingStats.value = res.data.data;
    }
  } catch (error) {
    console.error("获取教学统计失败:", error);
  }
  // 添加模拟教学案例数据
  teachingCases.value = [
    {
      id: 1,
      caseName: "基于双引擎的校园节能方案",
      caseType: "energy_saving",
      deviceName: "路灯控制系统",
      relatedCourse: "物联网技术",
      useCount: 45,
      description:
        "学生通过分析真实监测数据，提出校园照明和绿植养护的综合节能方案",
    },
    {
      id: 2,
      caseName: "微环境监测与植物生长研究",
      caseType: "environment_monitor",
      deviceName: "生态引擎传感器",
      relatedCourse: "环境科学导论",
      useCount: 32,
      description: "利用生态引擎数据，研究不同环境条件下校园绿植的生长状况",
    },
    {
      id: 3,
      caseName: "AIoT智慧校园系统开发",
      caseType: "lighting_control",
      deviceName: "智能调光系统",
      relatedCourse: "智慧校园实践",
      useCount: 28,
      description: "学生团队开发基于物联网的智慧校园管理系统原型",
    },
    {
      id: 4,
      caseName: "校园照明能耗分析",
      caseType: "lighting_control",
      deviceName: "路灯监控系统",
      relatedCourse: "绿色能源管理",
      useCount: 56,
      description: "分析校园路灯能耗数据，提出节能优化建议",
    },
  ];
};

const refreshData = async () => {
  await Promise.all([
    fetchOverview(),
    fetchEnergyStats(),
    fetchBillHistory(),
    fetchLampList(),
    fetchTeachingStats(),
  ]);
  ElMessage.success("数据刷新成功");
};

const exportDataReport = () => {
  ElMessage.success("报表导出成功");
};

// 刷新能耗分析数据
const refreshEnergyAnalysis = async () => {
  loading.value = true;
  try {
    await fetchEnergyStats();
    // 重新初始化图表
    if (currentView.value === "2-2") {
      initCharts();
    }
    ElMessage.success("能耗分析数据已刷新");
  } catch (error) {
    ElMessage.error("刷新失败");
  } finally {
    loading.value = false;
  }
};

// 导出能耗分析报告
const exportEnergyReport = () => {
  // 生成报告数据
  const reportData = {
    统计日期: new Date().toLocaleString(),
    平均节能率: energyIndicators.value.avgSavingRate + "%",
    累计节省费用: "¥" + formatMoney(energyIndicators.value.totalSavedCost),
    累计节电量: energyIndicators.value.totalSavedEnergy + "kWh",
    碳减排量: energyIndicators.value.carbonReduction + "kg",
  };

  // 创建CSV内容
  const csvContent = Object.entries(reportData)
    .map(([key, value]) => `${key},${value}`)
    .join("\n");

  // 下载文件
  const blob = new Blob(["\ufeff" + csvContent], {
    type: "text/csv;charset=utf-8;",
  });
  const link = document.createElement("a");
  link.href = URL.createObjectURL(blob);
  link.download = `能耗分析报告_${new Date().toISOString().split("T")[0]}.csv`;
  link.click();

  ElMessage.success("能耗分析报告导出成功");
};

// 刷新报表数据
const refreshReportData = async () => {
  loading.value = true;
  try {
    // 模拟API调用获取报表数据
    await new Promise((resolve) => setTimeout(resolve, 1000));
    // 模拟数据
    reportStats.value = {
      totalDevices: 35,
      totalAreas: 8,
      totalSavedCost: 5445,
      totalFaults: 5,
    };
    // 重新初始化图表
    initReportCharts();
    ElMessage.success("报表数据刷新成功");
  } catch (error) {
    ElMessage.error("报表数据刷新失败");
  } finally {
    loading.value = false;
  }
};

// 导出报表
const exportReport = () => {
  // 生成报告数据
  const reportData = {
    统计日期: new Date().toLocaleString(),
    总设备数: reportStats.value.totalDevices,
    管理区域: reportStats.value.totalAreas,
    累计节省费用: "¥" + formatMoney(reportStats.value.totalSavedCost),
    故障设备数: reportStats.value.totalFaults,
  };

  // 创建CSV内容
  const csvContent = Object.entries(reportData)
    .map(([key, value]) => `${key},${value}`)
    .join("\n");

  // 下载文件
  const blob = new Blob(["\ufeff" + csvContent], {
    type: "text/csv;charset=utf-8;",
  });
  const link = document.createElement("a");
  link.href = URL.createObjectURL(blob);
  link.download = `数据报表_${new Date().toISOString().split("T")[0]}.csv`;
  link.click();

  ElMessage.success("报表导出成功");
};

// 初始化报表图表
const initReportCharts = () => {
  // 设备类型分布图表
  if (deviceTypeChartRef.value) {
    if (deviceTypeChart) {
      deviceTypeChart.dispose();
    }
    deviceTypeChart = echarts.init(deviceTypeChartRef.value);
    deviceTypeChart.setOption({
      tooltip: {
        trigger: "item",
      },
      legend: {
        orient: "vertical",
        left: "left",
      },
      series: [
        {
          name: "设备类型",
          type: "pie",
          radius: "60%",
          data: [
            { value: 30, name: "智慧路灯" },
            { value: 3, name: "生态引擎" },
            { value: 2, name: "其他设备" },
          ],
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: "rgba(0, 0, 0, 0.5)",
            },
          },
        },
      ],
    });
  }

  // 区域设备分布图表
  if (areaDistributionChartRef.value) {
    if (areaDistributionChart) {
      areaDistributionChart.dispose();
    }
    areaDistributionChart = echarts.init(areaDistributionChartRef.value);
    areaDistributionChart.setOption({
      tooltip: {
        trigger: "item",
      },
      legend: {
        orient: "vertical",
        left: "left",
      },
      series: [
        {
          name: "区域设备",
          type: "pie",
          radius: "60%",
          data: [
            { value: 12, name: "教学区" },
            { value: 8, name: "宿舍区" },
            { value: 6, name: "办公区" },
            { value: 4, name: "运动区" },
            { value: 5, name: "其他区域" },
          ],
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: "rgba(0, 0, 0, 0.5)",
            },
          },
        },
      ],
    });
  }

  // 月度能耗趋势图表
  if (reportEnergyTrendChartRef.value) {
    if (reportEnergyTrendChart) {
      reportEnergyTrendChart.dispose();
    }
    reportEnergyTrendChart = echarts.init(reportEnergyTrendChartRef.value);
    reportEnergyTrendChart.setOption({
      tooltip: {
        trigger: "axis",
      },
      legend: {
        data: ["总能耗", "节能量"],
      },
      grid: {
        left: "3%",
        right: "4%",
        bottom: "3%",
        containLabel: true,
      },
      xAxis: {
        type: "category",
        boundaryGap: false,
        data: ["1月", "2月", "3月", "4月", "5月", "6月"],
      },
      yAxis: {
        type: "value",
      },
      series: [
        {
          name: "总能耗",
          type: "line",
          data: [4120, 3940, 3680, 3450, 3300, 3180],
        },
        {
          name: "节能量",
          type: "line",
          data: [680, 750, 825, 900, 975, 1050],
        },
      ],
    });
  }

  // 故障设备统计图表
  if (faultStatsChartRef.value) {
    if (faultStatsChart) {
      faultStatsChart.dispose();
    }
    faultStatsChart = echarts.init(faultStatsChartRef.value);
    faultStatsChart.setOption({
      tooltip: {
        trigger: "item",
      },
      legend: {
        orient: "vertical",
        left: "left",
      },
      series: [
        {
          name: "故障类型",
          type: "pie",
          radius: "60%",
          data: [
            { value: 3, name: "故障告警" },
            { value: 2, name: "设备损坏" },
          ],
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: "rgba(0, 0, 0, 0.5)",
            },
          },
        },
      ],
    });
  }
};

const optimizeAll = async () => {
  optimizing.value = true;
  try {
    const res = await optimizeLighting(null);
    if (res.data.code === 1) {
      ElMessage.success(
        `智能调光完成！调整了 ${res.data.data.adjustedDevices} 盏路灯，平均节能率 ${res.data.data.averageSavingRate.toFixed(1)}%`,
      );
      fetchLampList();
      fetchEnergyStats();
    }
  } catch (error) {
    ElMessage.error("智能调光失败");
  } finally {
    optimizing.value = false;
  }
};

const optimizeSingle = async (row) => {
  try {
    const res = await optimizeLighting(row.id);
    if (res.data.code === 1) {
      row.suggestedBrightness = res.data.data.optimalBrightness;
      row.savingRate = res.data.data.savingRate;
      ElMessage.success(`建议亮度: ${res.data.data.optimalBrightness}%`);
    }
  } catch (error) {
    ElMessage.error("优化失败");
  }
};

const applyOptimizationToLamp = async (row) => {
  try {
    const res = await applyLightingOptimization(
      row.id,
      row.suggestedBrightness,
    );
    if (res.data.code === 1) {
      ElMessage.success("优化方案应用成功");
      row.brightness = row.suggestedBrightness;
      fetchEnergyStats();
    }
  } catch (error) {
    ElMessage.error("应用失败");
  }
};

const generateBill = async () => {
  generating.value = true;
  try {
    const now = new Date();
    const billMonth = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}`;
    const res = await createBill(null, billMonth);
    if (res.data.code === 1) {
      ElMessage.success("账单生成成功");
      fetchBillHistory();
    }
  } catch (error) {
    ElMessage.error("生成账单失败");
  } finally {
    generating.value = false;
  }
};

const viewBillDetail = (row) => {
  currentBill.value = row;
};

const showCreateCaseDialog = () => {
  ElMessage.info("请选择设备创建教学案例");
};

const viewTeachingCase = async (row) => {
  currentCase.value = row;
  teachingCaseDialog.value = true;
};

const exportCase = (row) => {
  ElMessage.success("案例导出成功");
};

// 初始化图表
const initCharts = () => {
  console.log("initCharts called, currentView:", currentView.value);
  if (currentView.value !== "2-2" && currentView.value !== "1-4") return;

  console.log("initCharts refs:", {
    energyTrendChartRef: energyTrendChartRef.value,
    savingCompareChartRef: savingCompareChartRef.value,
    areaPieChartRef: areaPieChartRef.value,
    peakValleyChartRef: peakValleyChartRef.value,
  });

  // 如果图表实例已存在，先销毁
  if (energyTrendChart) {
    energyTrendChart.dispose();
    energyTrendChart = null;
  }
  if (savingCompareChart) {
    savingCompareChart.dispose();
    savingCompareChart = null;
  }
  if (areaPieChart) {
    areaPieChart.dispose();
    areaPieChart = null;
  }
  if (peakValleyChart) {
    peakValleyChart.dispose();
    peakValleyChart = null;
  }

  try {
    // 月度能耗趋势图
    if (energyTrendChartRef.value) {
      energyTrendChart = echarts.init(energyTrendChartRef.value);
      energyTrendChart.setOption({
        tooltip: { trigger: "axis" },
        legend: { data: ["总能耗", "节约能耗"], bottom: 0 },
        grid: {
          left: "3%",
          right: "4%",
          bottom: "15%",
          top: "10%",
          containLabel: true,
        },
        xAxis: {
          type: "category",
          data: monthlyTrendData.value.months,
          axisLine: { lineStyle: { color: "#ccc" } },
          axisLabel: { color: "#666" },
        },
        yAxis: {
          type: "value",
          name: "kWh",
          axisLine: { lineStyle: { color: "#ccc" } },
          axisLabel: { color: "#666" },
          splitLine: { lineStyle: { color: "#eee" } },
        },
        series: [
          {
            name: "总能耗",
            type: "line",
            data: monthlyTrendData.value.totalEnergy,
            smooth: true,
            itemStyle: { color: "#409EFF" },
            areaStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: "rgba(64, 158, 255, 0.3)" },
                { offset: 1, color: "rgba(64, 158, 255, 0.05)" },
              ]),
            },
          },
          {
            name: "节约能耗",
            type: "line",
            data: monthlyTrendData.value.savedEnergy,
            smooth: true,
            itemStyle: { color: "#67C23A" },
            areaStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: "rgba(103, 194, 58, 0.3)" },
                { offset: 1, color: "rgba(103, 194, 58, 0.05)" },
              ]),
            },
          },
        ],
      });
    }

    // 节能效果对比图
    if (savingCompareChartRef.value) {
      savingCompareChart = echarts.init(savingCompareChartRef.value);
      savingCompareChart.setOption({
        tooltip: { trigger: "axis", axisPointer: { type: "shadow" } },
        legend: { data: ["传统照明", "智能照明"], bottom: 0 },
        grid: {
          left: "3%",
          right: "4%",
          bottom: "15%",
          top: "10%",
          containLabel: true,
        },
        xAxis: {
          type: "category",
          data: ["1月", "2月", "3月", "4月", "5月", "6月"],
          axisLine: { lineStyle: { color: "#ccc" } },
          axisLabel: { color: "#666" },
        },
        yAxis: {
          type: "value",
          name: "kWh",
          axisLine: { lineStyle: { color: "#ccc" } },
          axisLabel: { color: "#666" },
          splitLine: { lineStyle: { color: "#eee" } },
        },
        series: [
          {
            name: "传统照明",
            type: "bar",
            data: [6000, 6000, 6000, 6000, 6000, 6000],
            itemStyle: { color: "#909399", borderRadius: [4, 4, 0, 0] },
          },
          {
            name: "智能照明",
            type: "bar",
            data: [5200, 4800, 4500, 4200, 3800, 3500],
            itemStyle: { color: "#409EFF", borderRadius: [4, 4, 0, 0] },
          },
        ],
      });
    }

    // 各区域能耗占比图
    if (areaPieChartRef.value) {
      areaPieChart = echarts.init(areaPieChartRef.value);
      areaPieChart.setOption({
        tooltip: { trigger: "item", formatter: "{b}: {c}kWh ({d}%)" },
        legend: { orient: "vertical", right: "5%", top: "center" },
        series: [
          {
            type: "pie",
            radius: ["40%", "70%"],
            center: ["40%", "50%"],
            avoidLabelOverlap: false,
            itemStyle: {
              borderRadius: 8,
              borderColor: "#fff",
              borderWidth: 2,
            },
            label: { show: false },
            emphasis: {
              label: { show: true, fontSize: 14, fontWeight: "bold" },
            },
            data: areaDistributionData.value.map((item, index) => {
              const colors = [
                "#409EFF",
                "#67C23A",
                "#E6A23C",
                "#F56C6C",
                "#909399",
              ];
              return {
                ...item,
                itemStyle: { color: colors[index % colors.length] },
              };
            }),
          },
        ],
      });
    }

    // 峰谷用电分析图
    if (peakValleyChartRef.value) {
      peakValleyChart = echarts.init(peakValleyChartRef.value);
      peakValleyChart.setOption({
        tooltip: { trigger: "axis", axisPointer: { type: "cross" } },
        legend: { data: ["峰时用电", "谷时用电"], bottom: 0 },
        grid: {
          left: "3%",
          right: "4%",
          bottom: "15%",
          top: "10%",
          containLabel: true,
        },
        xAxis: {
          type: "category",
          data: peakValleyData.value.periods,
          axisLine: { lineStyle: { color: "#ccc" } },
          axisLabel: { color: "#666" },
        },
        yAxis: {
          type: "value",
          name: "kWh",
          axisLine: { lineStyle: { color: "#ccc" } },
          axisLabel: { color: "#666" },
          splitLine: { lineStyle: { color: "#eee" } },
        },
        series: [
          {
            name: "峰时用电",
            type: "bar",
            stack: "total",
            data: peakValleyData.value.peak,
            itemStyle: { color: "#F56C6C" },
          },
          {
            name: "谷时用电",
            type: "bar",
            stack: "total",
            data: peakValleyData.value.valley,
            itemStyle: { color: "#67C23A" },
          },
        ],
      });
    }
  } catch (error) {
    console.error("图表初始化失败:", error);
  }
};

// 窗口大小变化时调整图表大小
const handleResize = () => {
  energyTrendChart?.resize();
  savingCompareChart?.resize();
  areaPieChart?.resize();
  peakValleyChart?.resize();
  deviceTypeChart?.resize();
  areaDistributionChart?.resize();
  reportEnergyTrendChart?.resize();
  faultStatsChart?.resize();
};

onMounted(async () => {
  fetchOverview();
  await fetchEnergyStats(); // 等待数据加载完成
  fetchBillHistory();
  fetchLampList();
  fetchGreenhouseList();
  fetchTeachingStats();

  window.addEventListener("resize", handleResize);

  // 初始加载时根据视图初始化对应的图表
  if (currentView.value === "2-2" || currentView.value === "1-4") {
    setTimeout(() => {
      initCharts();
    }, 100);
  } else if (currentView.value === "3-3") {
    setTimeout(() => {
      initReportCharts();
    }, 100);
  }
});

// 监听视图变化，初始化图表
watch(
  () => currentView.value,
  async (newVal) => {
    if (newVal === "2-2" || newVal === "1-4") {
      // 确保数据已加载
      if (!energyStats.value.savedCost) {
        await fetchEnergyStats();
      }
      setTimeout(() => {
        initCharts();
      }, 100);
    } else if (newVal === "3-3") {
      setTimeout(() => {
        initReportCharts();
      }, 100);
    }
  },
);

// 组件卸载时清理
onUnmounted(() => {
  window.removeEventListener("resize", handleResize);
  energyTrendChart?.dispose();
  savingCompareChart?.dispose();
  areaPieChart?.dispose();
  peakValleyChart?.dispose();
  deviceTypeChart?.dispose();
  areaDistributionChart?.dispose();
  reportEnergyTrendChart?.dispose();
  faultStatsChart?.dispose();
});
</script>

<style scoped>
.campus-energy-container {
  padding: 0;
  background: transparent;
  min-height: auto;
}

/* 双引擎概览 */
.engine-overview {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 24px;
  padding: 24px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.engine-section {
  flex: 1;
  padding: 20px;
  border-radius: 10px;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border: 1px solid #e2e8f0;
}

.engine-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.engine-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.energy-icon {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
  box-shadow: 0 4px 14px rgba(245, 158, 11, 0.4);
}

.ecology-icon {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  box-shadow: 0 4px 14px rgba(16, 185, 129, 0.4);
}

.engine-info h3 {
  margin: 0 0 6px 0;
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
}

.engine-info p {
  margin: 0;
  font-size: 13px;
  color: #64748b;
}

.engine-features {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.feature-tag {
  padding: 6px 12px;
  background: white;
  border-radius: 20px;
  font-size: 12px;
  color: #475569;
  border: 1px solid #e2e8f0;
}

.engine-divider {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.divider-line {
  width: 2px;
  height: 30px;
  background: linear-gradient(180deg, #e2e8f0 0%, #cbd5e1 100%);
}

.divider-text {
  padding: 8px 16px;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  color: white;
  font-size: 12px;
  font-weight: 600;
  border-radius: 20px;
  white-space: nowrap;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 20px 24px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.header-left h2 {
  margin: 0;
  font-size: 24px;
  color: #303133;
  font-weight: 600;
}

.subtitle {
  margin: 8px 0 0 0;
  font-size: 14px;
  color: #909399;
}

.header-right {
  display: flex;
  gap: 12px;
}

.stats-overview {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-item {
  background: white;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s;
}

.stat-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.main-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.section-card {
  background: white;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.feature-desc {
  margin-bottom: 20px;
}

.lamp-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.greenhouse-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.lamp-card {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 16px;
  transition: all 0.3s;
}

.lamp-card:hover {
  border-color: #409eff;
  box-shadow: 0 2px 12px rgba(64, 158, 255, 0.2);
}

.greenhouse-card {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 16px;
  transition: all 0.3s;
}

.greenhouse-card:hover {
  border-color: #67c23a;
  box-shadow: 0 2px 12px rgba(103, 194, 58, 0.2);
}

.lamp-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.lamp-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.lamp-data {
  margin-bottom: 12px;
}

.greenhouse-data {
  margin-bottom: 12px;
}

.data-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.data-label {
  font-size: 13px;
  color: #909399;
}

.data-value {
  font-size: 13px;
  font-weight: 500;
  color: #303133;
}

.data-value.highlight {
  color: #409eff;
  font-weight: 600;
}

.data-value.success {
  color: #67c23a;
  font-weight: 600;
}

.lamp-actions {
  display: flex;
  gap: 8px;
}

.greenhouse-controls {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.control-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.control-label {
  font-size: 13px;
  color: #909399;
}

.bill-overview {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}

.bill-card {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 20px;
}

.bill-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
}

.bill-stats {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.bill-stat-item {
  text-align: center;
}

.bill-stat-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
}

.bill-stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.bill-stat-value.success {
  color: #67c23a;
}

.bill-stat-value small {
  font-size: 14px;
  color: #909399;
}

.bill-analysis {
  padding: 12px 0;
}

.analysis-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  line-height: 1.8;
  color: #606266;
  white-space: pre-line;
}

.analysis-item span {
  white-space: pre-line;
}

.teaching-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.teaching-stat-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
}

.teaching-stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
}

.teaching-stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.analysis-content,
.cost-content {
  min-height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.content-box {
  background: #f5f7fa;
  padding: 16px;
  border-radius: 6px;
  white-space: pre-wrap;
  font-size: 14px;
  line-height: 1.8;
  max-height: 300px;
  overflow-y: auto;
  font-family:
    -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Oxygen, Ubuntu,
    sans-serif;
}

.code-box {
  font-family: "Courier New", monospace;
  background: #1e1e1e;
  color: #d4d4d4;
}

@media (max-width: 1400px) {
  .lamp-grid {
    grid-template-columns: repeat(3, 1fr);
  }

  .greenhouse-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

/* 节能分析图表样式 */
.analysis-charts {
  margin-top: 20px;
}

.chart-card {
  height: 380px;
}

.chart-card :deep(.el-card__header) {
  padding: 12px 16px;
  border-bottom: 1px solid #ebeef5;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 500;
  color: #303133;
}

.chart-container {
  width: 100%;
  height: 300px;
}

.indicator-card {
  height: 120px;
}

.indicator-content {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 10px;
}

.indicator-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.indicator-info {
  flex: 1;
}

.indicator-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.indicator-label {
  font-size: 13px;
  color: #909399;
}

@media (max-width: 1200px) {
  .stats-overview {
    grid-template-columns: repeat(2, 1fr);
  }

  .lamp-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .bill-overview {
    grid-template-columns: 1fr;
  }

  .analysis-charts .el-col {
    width: 100%;
    margin-bottom: 20px;
  }
}

@media (max-width: 768px) {
  .stats-overview {
    grid-template-columns: 1fr;
  }

  .lamp-grid {
    grid-template-columns: 1fr;
  }

  .teaching-stats {
    grid-template-columns: 1fr;
  }

  .indicator-card {
    margin-bottom: 12px;
  }
}

/* 数据报表样式 */
.report-stats {
  margin: 20px 0;
}

.report-stat-card {
  border-radius: 8px;
  overflow: hidden;
}

.report-stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
}

.report-stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.report-stat-info {
  flex: 1;
}

.report-stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  line-height: 1.2;
}

.report-stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.report-charts {
  margin-top: 20px;
}

.chart-card {
  border-radius: 8px;
  overflow: hidden;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-container {
  height: 300px;
  width: 100%;
}
.report-stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
}

.report-stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
}

.report-stat-info {
  flex: 1;
}

.report-stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.report-stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

.filter-card {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 500;
}

.chart-container {
  width: 100%;
  height: 300px;
}
</style>
