import React from "react";
import { useNavigate } from "react-router-dom";
import { createPageUrl } from "../../utils";
import "./CategoryCard.css";

// Material‑UI imports
import {
	Card,
	CardMedia,
	CardActionArea,
	Typography,
	Box,
} from "@mui/material";

export default function CategoryCard({ name, icon, imageUrl }) {
	const navigate = useNavigate();

	const handleClick = () =>
		navigate(createPageUrl("SearchResults") + `?category=${name.toLowerCase()}`);

	return (
		<Card className="category-card">
			<CardActionArea onClick={handleClick} className="category-card-action-area">
				<CardMedia
					component="img"
					height={160}
					image={imageUrl}
					alt={name}
				/>

				{/* Title overlay */}
				<Box className="category-card-overlay">
					{icon}
					<Typography variant="subtitle1" className="category-card-title">
						{name}
					</Typography>
				</Box>
			</CardActionArea>
		</Card>
	);
} 