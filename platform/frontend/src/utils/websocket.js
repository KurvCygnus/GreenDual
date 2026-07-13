const RECONNECT_INTERVAL = 3000
const PING_INTERVAL = 15000

let ws = null
let reconnectTimer = null
let pingTimer = null
let messageHandlers = []
let connectCallbacks = []
let disconnectCallbacks = []
let isManualClose = false

export function connectWebSocket(url)
{
    isManualClose = false
    if(ws && ws.readyState === WebSocket.OPEN) return

    try
    {
        ws = new WebSocket(url)
    }
    catch(e)
    {
        console.error('WebSocket 连接失败:', e)
        scheduleReconnect(url)
        return
    }

    ws.onopen = () =>
    {
        console.log('WebSocket 已连接')
        connectCallbacks.forEach(cb => cb())
        startPing()
    }

    ws.onmessage = (event) =>
    {
        try
        {
            const data = JSON.parse(event.data)
            messageHandlers.forEach(handler => handler(data))
        }
        catch(e)
        {
            console.warn('WebSocket 消息解析失败:', e)
        }
    }

    ws.onclose = () =>
    {
        console.log('WebSocket 已断开')
        disconnectCallbacks.forEach(cb => cb())
        stopPing()
        if(!isManualClose) scheduleReconnect(url)
    }

    ws.onerror = (e) =>
    {
        console.error('WebSocket 错误:', e)
        ws.close()
    }
}

function scheduleReconnect(url)
{
    if(isManualClose) return
    if(reconnectTimer) clearTimeout(reconnectTimer)
    reconnectTimer = setTimeout(() => connectWebSocket(url), RECONNECT_INTERVAL)
}

function startPing()
{
    stopPing()
    pingTimer = setInterval(
        () =>
        {
            if(ws && ws.readyState === WebSocket.OPEN) ws.send('ping')
        },
        PING_INTERVAL
    )
}

function stopPing()
{
    if(pingTimer)
    {
        clearInterval(pingTimer)
        pingTimer = null
    }
}

export function disconnectWebSocket()
{
    isManualClose = true
    if(reconnectTimer)
    {
        clearTimeout(reconnectTimer)
        reconnectTimer = null
    }
    stopPing()
    if(ws)
    {
        ws.close()
        ws = null
    }
}

export function onMessage(handler)
{
    messageHandlers.push(handler)
    return () => { messageHandlers = messageHandlers.filter(h => h !== handler) }
}

export function onConnect(callback)
{
    connectCallbacks.push(callback)
    return () => { connectCallbacks = connectCallbacks.filter(c => c !== callback) }
}

export function onDisconnect(callback)
{
    disconnectCallbacks.push(callback)
    return () => { disconnectCallbacks = disconnectCallbacks.filter(c => c !== callback) }
}
