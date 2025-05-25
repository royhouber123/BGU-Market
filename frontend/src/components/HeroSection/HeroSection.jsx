import React from "react";
import { Link } from "react-router-dom";
import { createPageUrl } from "../../utils";
import "./HeroSection.css";

// Material-UI imports
import { Box, Container, Typography, Button, Stack } from "@mui/material";
import ArrowRightAltIcon from "@mui/icons-material/ArrowRightAlt";

export default function HeroSection() {
	return (
		<Box className="hero-section-container">
			{/* Hero Image */}
			<Box className="hero-section-background" />

			{/* Hero Content */}
			<Box className="hero-section-content">
				<Container>
					<Box className="hero-section-text-container">
						<Typography
							variant="h2"
							component="h1"
							className="hero-section-title"
						>
							Find It, Love It, Buy It
						</Typography>
						<Typography
							variant="h5"
							className="hero-section-subtitle"
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
								className="hero-section-shop-btn"
							>
								Shop Now
							</Button>
							<Button
								component={Link}
								to={createPageUrl("Dashboard")}
								variant="outlined"
								size="large"
								endIcon={<ArrowRightAltIcon />}
								className="hero-section-sell-btn"
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