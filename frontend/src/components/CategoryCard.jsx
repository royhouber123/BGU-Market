import React from "react";
import { useNavigate } from "react-router-dom";
import { createPageUrl } from "../utils";

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
    <Card sx={{ borderRadius: 2, overflow: "hidden", boxShadow: 1 }}>
      <CardActionArea onClick={handleClick} sx={{ position: "relative" }}>
        <CardMedia
          component="img"
          height={160}
          image={imageUrl}
          alt={name}
        />

        {/* Title overlay */}
        <Box
          sx={{
            position: "absolute",
            bottom: 0,
            width: "100%",
            p: 1.5,
            display: "flex",
            alignItems: "center",
            bgcolor: "rgba(0,0,0,0.55)",
            color: "common.white",
          }}
        >
          {icon}
          <Typography variant="subtitle1" sx={{ ml: 1, fontWeight: 600 }}>
            {name}
          </Typography>
        </Box>
      </CardActionArea>
    </Card>
  );
}
