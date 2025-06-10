import React, { useState, useEffect, useRef } from "react";
import { Link as RouterLink, useNavigate } from "react-router-dom";
import { createPageUrl } from "../../utils";
import { useAuth } from "../../contexts/AuthContext";
import userService from "../../services/userService";
import notificationService from "../../services/notificationService";
import broadcaster from "../../notification/Broadcaster";
import "./Header.css";

// Add this import if using STOMP
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

import {
	AppBar,
	Toolbar,
	Container,
	Box,
	Typography,
	IconButton,
	Badge,
	Button,
	TextField,
	InputAdornment,
	Drawer,
	List,
	ListItem,
	ListItemButton,
	ListItemIcon,
	ListItemText,
	Divider,
	Popover,
	Snackbar,
	Alert,
} from "@mui/material";

import SearchIcon from "@mui/icons-material/Search";
import ShoppingCartIcon from "@mui/icons-material/ShoppingCart";
import AccountCircleIcon from "@mui/icons-material/AccountCircle";
import NotificationsIcon from "@mui/icons-material/Notifications";
import PersonIcon from "@mui/icons-material/Person";
import LogoutIcon from "@mui/icons-material/Logout";
import MenuIcon from "@mui/icons-material/Menu";
import CloseIcon from "@mui/icons-material/Close";

import AuthDialog from "../AuthDialog/AuthDialog";

export default function Header() {
    const navigate = useNavigate();
    const { currentUser, isAuthenticated, logout, cart } = useAuth();
    const [drawerOpen, setDrawerOpen] = useState(false);
    const [authOpen, setAuthOpen] = useState(false);
    const [notifications, setNotifications] = useState([]);
    const [unreadCount, setUnreadCount] = useState(0);
    const [notifAnchorEl, setNotifAnchorEl] = useState(null);
    const [toastOpen, setToastOpen] = useState(false);
    const [toastMessage, setToastMessage] = useState('');
    const wsRef = useRef(null);

    
    const handleSearch = (e) => {
        e.preventDefault();
        const query = new FormData(e.currentTarget).get("search")?.toString().trim();
        if (query) navigate(createPageUrl("SearchResults") + `?q=${encodeURIComponent(query)}`);
    };

    const handleLogout = async () => {
        try {
            await logout();
            navigate('/'); // Redirect to home after logout
        } catch (error) {
            console.error('Logout failed:', error);
        }
    };

    const handleNotifClick = (event) => {
        setNotifAnchorEl(event.currentTarget);
    };

    const handleNotifClose = () => {
        setNotifAnchorEl(null);
    };

    const handleNotificationClick = async (notif) => {
        if (!notif.read) {
            try {
                await notificationService.markAsRead(currentUser.userName, notif.id);
                setNotifications((prev) =>
                    prev.map((n) => n.id === notif.id ? { ...n, read: true } : n)
                );
                setUnreadCount((prev) => Math.max(0, prev - 1));
            } catch (e) {
                console.error("Failed to mark notification as read", e);
            }
        }
        handleNotifClose();
    };

    const handleMarkAllAsRead = async () => {
        try {
            // Mark all unread notifications as read in the backend
            await Promise.all(
                notifications
                    .filter(n => !n.read)
                    .map(n => notificationService.markAsRead(currentUser.userName, n.id))
            );
            // Update state in frontend
            setNotifications(prev => prev.map(n => ({ ...n, read: true })));
            setUnreadCount(0);
        } catch (e) {
            console.error("Failed to mark all notifications as read", e);
        }
    };

    // Fetch notifications when authenticated
    useEffect(() => {
        const fetchNotifications = async () => {
            if (isAuthenticated && currentUser && currentUser.userName) {
                try {
                    const data = await notificationService.getNotifications(currentUser.userName);
                    setNotifications(data);
                    setUnreadCount(data.filter(n => !n.read).length);
                } catch (e) {
                    console.error("Failed to fetch notifications", e);
                }
            }
        };
        fetchNotifications();
    }, [isAuthenticated, currentUser]);

    //notification broadcaster
    // Register for notifications using the broadcaster
    useEffect(() => {
        if (!currentUser || !currentUser.userName) return;
        const remove = broadcaster.register(currentUser.userName, (message) => {
            setNotifications(prev => [message, ...prev]);
            setUnreadCount(prev => prev + 1);
        });
        return () => remove();
    }, [currentUser && currentUser.userName]);


    // Example using STOMP over WebSocket
    useEffect(() => {
        if (isAuthenticated && currentUser && currentUser.userName) {
            const socket = new SockJS('http://localhost:8080/ws/notifications');
            const stompClient = new Client({
                webSocketFactory: () => socket,
                onConnect: (frame) => {
                    stompClient.subscribe('/topic/notifications', (message) => {
                        const rawNotification = JSON.parse(message.body);
                        
                        let notification;
                        if (rawNotification.message && typeof rawNotification.message === 'string') {
                            try {
                                notification = JSON.parse(rawNotification.message);
                            } catch (e) {
                                notification = rawNotification;
                            }
                        } else {
                            notification = rawNotification;
                        }
                        
                        const targetUser = notification.userName || notification.userId;
                        if (targetUser === currentUser.userName) {
                            setNotifications(prev => [notification, ...prev]);
                            setUnreadCount(prev => prev + 1);
                            
                            // Show toast notification
                            setToastMessage(notification.message);
                            setToastOpen(true);
                        }
                    });
                },
                onStompError: (frame) => {
                    console.error('STOMP error:', frame);
                },
                onWebSocketError: (error) => {
                    console.error('WebSocket error:', error);
                }
            });
            
            stompClient.activate();
            
            return () => {
                if (stompClient.active) {
                    stompClient.deactivate();
                }
            };
        }
    }, [isAuthenticated, currentUser]);


    const navItems = [
        {
            label: "Cart",
            icon: <ShoppingCartIcon />,
            action: () => navigate(createPageUrl("Cart")),
            badge: cart.reduce((sum, i) => sum + i.quantity, 0),
        },
        {
            label: "Profile",
            icon: <AccountCircleIcon />,
            action: () => navigate(createPageUrl("Profile")),
        },
        {
            label: "Notifications",
            icon: (
                <Badge badgeContent={unreadCount} color="error">
                    <NotificationsIcon />
                </Badge>
            ),
            action: handleNotifClick,
        },
    ];

    // Handle toast close
    const handleToastClose = (event, reason) => {
        if (reason === 'clickaway') {
            return;
        }
        setToastOpen(false);
    };

    return (
        <>
            <AppBar
                position="sticky"
                color="default"
                elevation={0}
                className="header-appbar"
            >
                <Container maxWidth="lg">
                    <Toolbar disableGutters className="header-toolbar">
                        {/* ——— Logo ——— */}
                        <Box
                            component={RouterLink}
                            to={createPageUrl("Dashboard")}
                            className="header-logo-link"
                        >
                            <Typography
                                variant="h6"
                                fontWeight={800}
                                className="header-logo-text"
                                noWrap
                            >
                                BGU‑Marketplace
                            </Typography>
                        </Box>

                        {/* ——— Search (md+) ——— */}
                        <Box
                            component="form"
                            onSubmit={handleSearch}
                            className="header-search-desktop header-hide-mobile"
                        >
                            <TextField
                                name="search"
                                placeholder="Search for anything…"
                                size="small"
                                fullWidth
                                variant="outlined"
                                InputProps={{
                                    endAdornment: (
                                        <InputAdornment position="end">
                                            <IconButton type="submit" edge="end">
                                                <SearchIcon fontSize="small" />
                                            </IconButton>
                                        </InputAdornment>
                                    ),
                                }}
                            />
                        </Box>

                        {/* ——— Desktop actions ——— */}
                        <Box className="header-desktop-actions header-hide-mobile">
                            {navItems.map(({ label, icon, action, badge }) => (
                                <IconButton key={label} onClick={action} size="large">
                                    {icon}
                                </IconButton>
                            ))}

                            {/* Authentication Button */}
                            {isAuthenticated && currentUser ? (
                                <Box className="header-user-info">
                                    <Typography variant="body2" className="header-user-greeting">
                                        Hello, {currentUser.userName || 'User'}
                                    </Typography>
                                    <Button
                                        variant="outlined"
                                        color="primary"
                                        startIcon={<LogoutIcon />}
                                        onClick={handleLogout}
                                        className="header-auth-button"
                                    >
                                        Logout
                                    </Button>
                                </Box>
                            ) : (
                                <Button
                                    variant="contained"
                                    color="primary"
                                    startIcon={<PersonIcon />}
                                    onClick={() => setAuthOpen(true)}
                                    className="header-auth-button"
                                >
                                    Sign in
                                </Button>
                            )}
                        </Box>

                        {/* ——— Mobile hamburger ——— */}
                        <IconButton
                            onClick={() => setDrawerOpen(true)}
                            className="header-mobile-menu header-hide-desktop"
                            size="large"
                        >
                            <MenuIcon />
                        </IconButton>
                    </Toolbar>

                    {/* ——— Mobile search (xs–sm) ——— */}
                    <Box
                        component="form"
                        onSubmit={handleSearch}
                        className="header-search-mobile header-show-mobile"
                    >
                        <TextField
                            name="search"
                            placeholder="Search for anything…"
                            size="small"
                            fullWidth
                            variant="outlined"
                            InputProps={{
                                endAdornment: (
                                    <InputAdornment position="end">
                                        <IconButton type="submit" edge="end" size="small">
                                            <SearchIcon fontSize="small" />
                                        </IconButton>
                                    </InputAdornment>
                                ),
                            }}
                        />
                    </Box>
                </Container>
            </AppBar>

            {/* ——— Mobile drawer ——— */}
            <Drawer anchor="right" open={drawerOpen} onClose={() => setDrawerOpen(false)}>
                <Box className="header-drawer-content" role="presentation">
                    <List>
                        {navItems.map(({ label, icon, action, badge }) => (
                            <ListItemButton key={label} onClick={() => { action(); setDrawerOpen(false); }}>
                                <ListItemIcon>
                                    {badge !== undefined ? (
                                        <Badge badgeContent={badge} color="error">
                                            {icon}
                                        </Badge>
                                    ) : (
                                        icon
                                    )}
                                </ListItemIcon>
                                <ListItemText primary={label} />
                            </ListItemButton>
                        ))}
                    </List>
                    <Divider />
                    <List>
                        {isAuthenticated && currentUser ? (
                            <>
                                <ListItemButton disabled>
                                    <ListItemIcon>
                                        <PersonIcon />
                                    </ListItemIcon>
                                    <ListItemText primary={`Hello, ${currentUser.userName || 'User'}`} />
                                </ListItemButton>
                                <ListItemButton onClick={() => { handleLogout(); setDrawerOpen(false); }}>
                                    <ListItemIcon>
                                        <LogoutIcon />
                                    </ListItemIcon>
                                    <ListItemText primary="Logout" />
                                </ListItemButton>
                            </>
                        ) : (
                            <ListItemButton onClick={() => { setAuthOpen(true); setDrawerOpen(false); }}>
                                <ListItemIcon>
                                    <PersonIcon />
                                </ListItemIcon>
                                <ListItemText primary="Sign in" />
                            </ListItemButton>
                        )}
                    </List>
                </Box>
            </Drawer>

            {/* ——— Notifications Popover ——— */}
            <Popover
                open={Boolean(notifAnchorEl)}
                anchorEl={notifAnchorEl}
                onClose={handleNotifClose}
                anchorOrigin={{
                    vertical: 'bottom',
                    horizontal: 'right',
                }}
                transformOrigin={{
                    vertical: 'top',
                    horizontal: 'right',
                }}
            >
                <Box sx={{
                    minWidth: 350,
                    maxWidth: 400,
                    p: 0,
                    position: 'relative',
                    display: 'flex',
                    flexDirection: 'column',
                    height: 400 // or any max height you want for the popover
                }}>
                    <Box sx={{
                        flex: 1,
                        overflowY: 'auto',
                        p: 2,
                        pb: 8,
                        minHeight: 0 // ensures flexbox works with overflow
                    }}>
                        {notifications.length === 0 ? (
                            <Typography sx={{ p: 2 }}>No notifications</Typography>
                        ) : (
                            notifications
                                .slice()
                                .sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp))
                                .map((notif) => (
                                    <ListItem
                                        button
                                        key={notif.id}
                                        onClick={() => handleNotificationClick(notif)}
                                        selected={!notif.read}
                                        sx={{ bgcolor: notif.read ? 'inherit' : 'rgba(25,118,210,0.08)', pr: 4, position: 'relative' }}
                                    >
                                        <ListItemText
                                            primary={notif.message}
                                            secondary={new Date(notif.timestamp).toLocaleString()}
                                        />
                                    </ListItem>
                                ))
                        )}
                    </Box>
                    <Divider sx={{ m: 0 }} />
                    <Box sx={{
                        position: 'sticky',
                        bottom: 0,
                        left: 0,
                        width: '100%',
                        bgcolor: 'background.paper',
                        p: 2,
                        boxShadow: '0 -2px 8px rgba(0,0,0,0.04)'
                    }}>
                        {notifications.length > 0 && unreadCount > 0 && (
                            <Button
                                onClick={handleMarkAllAsRead}
                                size="small"
                                sx={{ m: 1, float: 'right' }}
                            >
                                Mark all as read
                            </Button>
                        )}
                        <Button
                            component={RouterLink}
                            to="/notifications/history"
                            fullWidth
                            size="small"
                        >
                            View all notification history
                        </Button>
                    </Box>
                </Box>
            </Popover>

            {/* ——— Toast Notification ——— */}
            <Snackbar
                open={toastOpen}
                autoHideDuration={3000}
                onClose={handleToastClose}
                anchorOrigin={{ 
                    vertical: 'top', 
                    horizontal: 'right' 
                }}
                sx={{
                    mt: 8, // Add margin top to position below header
                }}
            >
                <Alert 
                    onClose={handleToastClose} 
                    severity="info" 
                    variant="filled"
                    sx={{
                        minWidth: 300,
                        maxWidth: 400,
                    }}
                >
                    <Typography variant="body2" component="div">
                        <strong>New Notification</strong>
                    </Typography>
                    <Typography variant="body2" component="div">
                        {toastMessage}
                    </Typography>
                </Alert>
            </Snackbar>

            {/* ——— Auth dialog ——— */}
            <AuthDialog open={authOpen} onOpenChange={setAuthOpen} />
        </>
    );

    
}