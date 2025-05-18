import React from "react";
import { Link } from "react-router-dom";
import { createPageUrl } from "../utils";

// Material-UI imports
import { Box, Container, Typography, Button, Stack } from "@mui/material";
import ArrowRightAltIcon from "@mui/icons-material/ArrowRightAlt";

export default function HeroSection() {
  return (
    <Box
      sx={{
        position: 'relative',
        height: '450px',
        overflow: 'hidden',
        my: 2,
        borderRadius: 4,
      }}
    >
      {/* Hero Image */}
      <Box
        sx={{
          position: 'absolute',
          inset: 0,
          backgroundImage: "url('https://images.unsplash.com/photo-1550745165-9bc0b252726f?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=2070&q=80')",
          backgroundPosition: 'center 30%',
          backgroundSize: 'cover',
          '&::after': {
            content: '""',
            position: 'absolute',
            inset: 0,
            background: 'linear-gradient(to right, rgba(0,0,0,0.7), transparent)'
          }
        }}
      />

      {/* Hero Content */}
      <Box
        sx={{
          position: 'relative',
          height: '100%',
          display: 'flex',
          alignItems: 'center'
        }}
      >
        <Container>
          <Box sx={{ maxWidth: '600px' }}>
            <Typography
              variant="h2"
              component="h1"
              sx={{
                fontWeight: 'bold',
                color: 'white',
                mb: 2,
                fontSize: { xs: '2.25rem', md: '3rem' }
              }}
            >
              Find It, Love It, Buy It
            </Typography>
            <Typography
              variant="h5"
              sx={{ color: 'rgba(255, 255, 255, 0.9)', mb: 4 }}
            >
              Discover unique items from thousands of sellers around the world
            </Typography>
            <Stack
              direction={{ xs: 'column', sm: 'row' }}
              spacing={2}
            >
              <Button
                variant="contained"
                size="large"
                sx={{
                  bgcolor: '#2563eb',
                  '&:hover': { bgcolor: '#1d4ed8' },
                  fontWeight: 600,
                  boxShadow: 3
                }}
              >
                Shop Now
              </Button>
              <Button
                component={Link}
                to={createPageUrl("Home")}
                variant="outlined"
                size="large"
                endIcon={<ArrowRightAltIcon />}
                sx={{
                  bgcolor: 'rgba(255, 255, 255, 0.1)',
                  backdropFilter: 'blur(4px)',
                  borderColor: 'rgba(255, 255, 255, 0.3)',
                  color: 'white',
                  '&:hover': { bgcolor: 'rgba(255, 255, 255, 0.2)' }
                }}
              >
                Sell Your Items
              </Button>
            </Stack>
          </Box>
        </Container>
      </Box>
    </Box>
  );
}
