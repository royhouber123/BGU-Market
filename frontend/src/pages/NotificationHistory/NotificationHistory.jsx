import React, { useEffect, useState } from "react";
import { Button, Stack } from "@mui/material";
import { useAuth } from "../../contexts/AuthContext";
import notificationService from "../../services/notificationService";
import purchaseService from "../../services/purchaseService";
import { Box, Typography, List, ListItem, ListItemText, Divider } from "@mui/material";

export default function NotificationHistory() {
  const { currentUser, isAuthenticated } = useAuth();
  const [notifications, setNotifications] = useState([]);

  useEffect(() => {
    const fetchAll = async () => {
      if (isAuthenticated && currentUser?.userName) {
        const data = await notificationService.getNotifications(currentUser.userName);
        setNotifications(data);
      }
    };
    fetchAll();
  }, [isAuthenticated, currentUser]);

  // Extract storeId & productId from notification message if they are not directly present
  const extractIds = (notif) => {
    let { storeId, productId } = notif;
    if ((!storeId || !productId) && notif.message) {
      // Try new counter offer format: "Counter offer proposed: X for product Y at store Z"
      const counterOfferRegex = /Counter offer proposed: .+ for product (\S+) at store (\S+)/i;
      const counterMatch = notif.message.match(counterOfferRegex);
      if (counterMatch) {
        productId = productId || counterMatch[1];
        storeId = storeId || counterMatch[2];
      } else {
        // Try old format: "for product X at store Y"
        const oldFormatRegex = /for product (\S+) at store (\S+)/i;
        const oldMatch = notif.message.match(oldFormatRegex);
        if (oldMatch) {
          productId = productId || oldMatch[1];
          storeId = storeId || oldMatch[2];
        }
      }
    }
    return { storeId, productId };
  };

  const handleAccept = async (notif) => {
    const { storeId, productId } = extractIds(notif);
    if (!storeId || !productId) {
      console.warn("Cannot accept counter offer – missing IDs", notif);
      return;
    }
    try {
      await purchaseService.acceptCounterOffer(storeId, productId);
      // Mark notification as read
      await notificationService.markAsRead(currentUser.userName, notif.id);
      // Update local state to reflect the read status
      setNotifications(prev => prev.map(n =>
        n.id === notif.id ? { ...n, read: true } : n
      ));
    } catch (err) {
      console.error("Accept counter offer failed", err);
      alert(err.message || "Failed to accept counter offer");
    }
  };

  const handleDecline = async (notif) => {
    const { storeId, productId } = extractIds(notif);
    if (!storeId || !productId) {
      console.warn("Cannot decline counter offer – missing IDs", notif);
      return;
    }
    try {
      await purchaseService.declineCounterOffer(storeId, productId);
      // Mark notification as read
      await notificationService.markAsRead(currentUser.userName, notif.id);
      // Update local state to reflect the read status
      setNotifications(prev => prev.map(n =>
        n.id === notif.id ? { ...n, read: true } : n
      ));
    } catch (err) {
      console.error("Decline counter offer failed", err);
      alert(err.message || "Failed to decline counter offer");
    }
  };

  const isCounterOffer = (notif) => {
    if (!notif) return false;
    if (notif.type && String(notif.type).toUpperCase() === "COUNTER_OFFER") return true;
    return String(notif.message || "").toLowerCase().includes("counter offer");
  };

  return (
    <Box sx={{ maxWidth: 600, mx: "auto", mt: 4 }}>
      <Typography variant="h5" gutterBottom>
        Notification History
      </Typography>
      <Divider sx={{ mb: 2 }} />
      <List>
        {notifications.length === 0 ? (
          <Typography>No notifications found.</Typography>
        ) : (
          notifications
            .slice()
            .sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp))
            .map((notif) => (
              <ListItem key={notif.id} sx={{ bgcolor: notif.read ? "inherit" : "rgba(25,118,210,0.08)" }}>
                <ListItemText
                  primary={notif.message}
                  secondary={new Date(notif.timestamp).toLocaleString()}
                />
                {isCounterOffer(notif) && !notif.read && (
                  <Stack direction="row" spacing={1} sx={{ ml: 2 }}>
                    <Button
                      variant="contained"
                      color="primary"
                      size="small"
                      onClick={() => handleAccept(notif)}
                    >
                      Accept
                    </Button>
                    <Button
                      variant="outlined"
                      color="error"
                      size="small"
                      onClick={() => handleDecline(notif)}
                    >
                      Decline
                    </Button>
                  </Stack>
                )}
              </ListItem>
            ))
        )}
      </List>
    </Box>
  );
}