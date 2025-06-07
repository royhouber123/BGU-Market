import React, { useEffect, useState } from "react";
import { useAuth } from "../contexts/AuthContext";
import notificationService from "../services/notificationService";
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
              </ListItem>
            ))
        )}
      </List>
    </Box>
  );
}