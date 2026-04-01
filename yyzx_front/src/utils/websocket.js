// utils/websocket.js
class WebSocketClient {
    constructor() {
      this.socket = null;
      this.userId = null;
      this.reconnectTimer = null;
      this.heartbeatTimer = null;
      this.reconnectCount = 0;//重连次数
      this.maxReconnectCount = 5;//最大重连次数
      this.heartbeatInterval = 30000;//心跳间隔
      this.onMessageCallback = null;//接收消息的回调函数
      this.onNotificationCallback = null;//通知消息的回调函数
    }
  
    connect(userId) {
      if (this.socket && this.socket.readyState === WebSocket.OPEN) {
        console.log('WebSocket 已连接');
        return;
      }
  
      this.disconnect();
  
      const wsUrl = `ws://localhost:9999/yyzx/ws/${userId}`;
      console.log('🔗 尝试连接 WebSocket:', wsUrl);
      
      this.socket = new WebSocket(wsUrl);
      this.userId = userId;
  
      this.socket.onopen = () => {
        console.log('✅ WebSocket 连接成功');
        this.reconnectCount = 0;
        this.startHeartbeat();
      };
  
      this.socket.onmessage = (event) => {
        try {
          const message = JSON.parse(event.data);
          console.log('📨 收到消息：', message);
          this.handleMessage(message);
        } catch (error) {
          console.error('❌ 消息解析失败:', error);
        }
      };
  
      this.socket.onerror = (error) => {
        console.error('❌ WebSocket 错误:', error);
      };
  
      this.socket.onclose = (event) => {
        console.log('🔴 WebSocket 连接关闭:', event.code, event.reason);
        this.stopHeartbeat();
        this.scheduleReconnect();
      };
    }
  
    handleMessage(message) {
      if (this.onMessageCallback) {
        this.onMessageCallback(message);
      }
  
      switch(message.type) {
        case 1:
          this.showNotification('登录成功', message.message, 'success');
          break;
        case 2:
          this.showNotification('密码修改', message.message, 'success');
          break;
        case 3:
          this.showNotification('系统警告', message.message, 'warning');
          break;
        default:
          console.log('未知消息类型:', message.type);
      }
    }
    // 显示通知
    showNotification(title, content, type = 'info') {
      if (this.onNotificationCallback) {
        this.onNotificationCallback(title, content, type);
      }
    }
    // 接收消息
    onMessage(callback) {
      this.onMessageCallback = callback;
    }
    // 接收通知
    onNotification(callback) {
      this.onNotificationCallback = callback;
    }
    // 启动心跳
    startHeartbeat() {
      this.stopHeartbeat();
      this.heartbeatTimer = setInterval(() => {
        if (this.socket && this.socket.readyState === WebSocket.OPEN) {
          this.socket.send(JSON.stringify({ type: 'heartbeat', timestamp: Date.now() }));
        }
      }, this.heartbeatInterval);
    }
    // 停止心跳
    stopHeartbeat() {
      if (this.heartbeatTimer) {
        clearInterval(this.heartbeatTimer);
        this.heartbeatTimer = null;
      }
    }
  
    scheduleReconnect() {
      if (this.reconnectCount >= this.maxReconnectCount) {
        console.log('⚠️ 达到最大重连次数，停止重连');
        this.showNotification('连接断开', '服务器连接失败，请刷新页面', 'error');
        return;
      }
  
      if (this.reconnectTimer) {
        clearTimeout(this.reconnectTimer);
      }
  
      const delay = Math.min(1000 * Math.pow(2, this.reconnectCount), 30000);
      
      console.log(`🔄 ${delay/1000}秒后尝试第${this.reconnectCount + 1}次重连...`);
      
      this.reconnectTimer = setTimeout(() => {
        this.reconnectCount++;
        if (this.userId) {
          this.connect(this.userId);
        }
      }, delay);
    }
  
    send(data) {
      if (this.socket && this.socket.readyState === WebSocket.OPEN) {
        this.socket.send(JSON.stringify(data));
      } else {
        console.error('❌ WebSocket 未连接，无法发送消息');
      }
    }
  
    disconnect() {
      this.stopHeartbeat();
      
      if (this.reconnectTimer) {
        clearTimeout(this.reconnectTimer);
        this.reconnectTimer = null;
      }
  
      if (this.socket) {
        this.socket.close();
        this.socket = null;
      }
      
      this.userId = null;
      this.reconnectCount = 0;
    }
  
    isConnected() {
      return this.socket && this.socket.readyState === WebSocket.OPEN;
    }
  }
  
  export default new WebSocketClient();