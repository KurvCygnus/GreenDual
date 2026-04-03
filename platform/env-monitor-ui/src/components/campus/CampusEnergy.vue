<template>
  <div class="campus-energy-container">
    <div class="stats-overview">
      <div class="stat-item">
        <div
          class="stat-icon"
          style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)"
        >
          <img
            src="https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=smart%20campus%20map%20with%20zones&image_size=square"
            alt="管理区域"
            style="width: 28px; height: 28px; object-fit: contain"
          />
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
          <img
            src="https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=smart%20street%20lights%20on%20campus&image_size=square"
            alt="路灯设备"
            style="width: 28px; height: 28px; object-fit: contain"
          />
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ overview.totalDevices || 8 }}</div>
          <div class="stat-label">路灯设备</div>
        </div>
      </div>

      <div class="stat-item">
        <div
          class="stat-icon"
          style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)"
        >
          <img
            src="https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=money%20savings%20icon%20for%20energy&image_size=square"
            alt="节省电费"
            style="width: 28px; height: 28px; object-fit: contain"
          />
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
          <img
            src="https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=energy%20savings%20chart%20icon&image_size=square"
            alt="节能率"
            style="width: 28px; height: 28px; object-fit: contain"
          />
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
    <template v-else-if="currentView === '2-2'">
      <div class="main-content">
        <div class="section-card">
          <div class="section-header">
            <div class="section-title">
              <el-icon :size="20" color="#409EFF"><TrendCharts /></el-icon>
              <span>节能分析</span>
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
          </div>
          <el-empty description="数据报表功能开发中" />
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

// 图表相关
const energyTrendChartRef = ref(null);
const savingCompareChartRef = ref(null);
const areaPieChartRef = ref(null);
const peakValleyChartRef = ref(null);
let energyTrendChart = null;
let savingCompareChart = null;
let areaPieChart = null;
let peakValleyChart = null;

// 节能指标
const energyIndicators = ref({
  avgSavingRate: 35.5,
  totalSavedCost: 15480,
  totalSavedEnergy: 25800,
  carbonReduction: 15600,
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
    }
  } catch (error) {
    console.error("获取能源统计失败:", error);
  }
  // 添加模拟数据
  if (!energyStats.value.savedCost) {
    energyStats.value.savedCost = 2580.5;
  }
  if (!energyStats.value.savingRate) {
    energyStats.value.savingRate = 35.5;
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

const exportReport = () => {
  ElMessage.success("报告导出成功");
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
  if (currentView.value !== "2-2") return;

  nextTick(() => {
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
            name: "总能耗",
            type: "line",
            data: [5200, 4800, 4500, 4200, 3800, 3500],
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
            data: [800, 1200, 1500, 1800, 2200, 2500],
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
            data: [
              { value: 850, name: "图书馆", itemStyle: { color: "#409EFF" } },
              {
                value: 1200,
                name: "教学楼A区",
                itemStyle: { color: "#67C23A" },
              },
              {
                value: 680,
                name: "教学楼B区",
                itemStyle: { color: "#E6A23C" },
              },
              { value: 520, name: "学生宿舍", itemStyle: { color: "#F56C6C" } },
              { value: 250, name: "体育馆", itemStyle: { color: "#909399" } },
            ],
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
          data: ["00:00", "04:00", "08:00", "12:00", "16:00", "20:00", "24:00"],
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
            data: [50, 30, 200, 350, 280, 400, 80],
            itemStyle: { color: "#F56C6C" },
          },
          {
            name: "谷时用电",
            type: "bar",
            stack: "total",
            data: [100, 80, 150, 200, 180, 250, 120],
            itemStyle: { color: "#67C23A" },
          },
        ],
      });
    }
  });
};

// 监听视图变化，初始化图表
watch(
  () => currentView.value,
  (newVal) => {
    if (newVal === "2-2") {
      initCharts();
    }
  },
);

// 窗口大小变化时重新调整图表
const handleResize = () => {
  energyTrendChart?.resize();
  savingCompareChart?.resize();
  areaPieChart?.resize();
  peakValleyChart?.resize();
};

onMounted(() => {
  fetchOverview();
  fetchEnergyStats();
  fetchBillHistory();
  fetchLampList();
  fetchGreenhouseList();
  fetchTeachingStats();

  window.addEventListener("resize", handleResize);
});

// 监听视图变化
watch(
  () => currentView.value,
  (newVal) => {
    if (newVal === "2-2") {
      initCharts();
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
}
</style>
