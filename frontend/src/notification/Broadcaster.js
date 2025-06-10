class Broadcaster {
  constructor() {
    this.listeners = {};
  }

  register(userId, listener) {
    if (!this.listeners[userId]) {
      this.listeners[userId] = [];
    }
    this.listeners[userId].push(listener);

    // Return a function to remove the listener
    return () => {
      this.listeners[userId] = this.listeners[userId].filter(l => l !== listener);
      if (this.listeners[userId].length === 0) {
        delete this.listeners[userId];
      }
    };
  }

  broadcast(userId, message) {
    const userListeners = this.listeners[userId] || [];
    userListeners.forEach(listener => listener(message));
  }
}

const broadcaster = new Broadcaster();
export default broadcaster;