const https = require('https');

const DEEPSEEK_API_KEY = 'sk-d3447a51c4c349c7bd269d1c9654cb25';
const DEEPSEEK_API_URL = 'api.deepseek.com';

function testDeepSeekAPI() {
  const data = JSON.stringify({
    model: 'deepseek-chat',
    messages: [
      {
        role: 'system',
        content: '你是一个测试助手。'
      },
      {
        role: 'user',
        content: '请回复"连接成功"，确认API正常工作。'
      }
    ],
    temperature: 0.7,
    max_tokens: 100
  });

  const options = {
    hostname: DEEPSEEK_API_URL,
    port: 443,
    path: '/v1/chat/completions',
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${DEEPSEEK_API_KEY}`,
      'Content-Length': Buffer.byteLength(data)
    }
  };

  console.log('🚀 开始测试DeepSeek API...');
  console.log('📡 API URL:', `https://${DEEPSEEK_API_URL}/v1/chat/completions`);
  console.log('🔑 API Key:', DEEPSEEK_API_KEY.substring(0, 10) + '...');
  console.log('');

  const req = https.request(options, (res) => {
    console.log(`✅ 响应状态码: ${res.statusCode}`);
    console.log('📋 响应头:', JSON.stringify(res.headers, null, 2));
    console.log('');

    let responseData = '';

    res.on('data', (chunk) => {
      responseData += chunk;
    });

    res.on('end', () => {
      try {
        const jsonResponse = JSON.parse(responseData);
        console.log('📦 响应数据:', JSON.stringify(jsonResponse, null, 2));
        console.log('');
        
        if (jsonResponse.choices && jsonResponse.choices.length > 0) {
          console.log('🤖 AI回复:', jsonResponse.choices[0].message.content);
          console.log('');
          console.log('✅✅✅ DeepSeek API 连接成功！✅✅✅');
        } else {
          console.log('❌ 响应格式异常');
        }
      } catch (error) {
        console.log('❌ JSON解析失败:', error.message);
        console.log('原始响应:', responseData);
      }
    });
  });

  req.on('error', (error) => {
    console.error('❌ 请求失败:', error.message);
    console.error('错误详情:', error);
  });

  req.write(data);
  req.end();
}

console.log('='.repeat(60));
console.log('DeepSeek API 连接测试');
console.log('='.repeat(60));
console.log('');

testDeepSeekAPI();