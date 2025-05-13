import React from "react";
import { Link, useNavigate } from "react-router-dom";
import { createPageUrl } from "@/utils";
import { motion } from "framer-motion";

export default function CategoryCard({ name, icon, imageUrl }) {
  const navigate = useNavigate();

  const handleClick = (e) => {
    e.preventDefault();
    navigate(
      createPageUrl("SearchResults") + `?category=${name.toLowerCase()}`
    );
  };

  return (
    <motion.div
      whileHover={{ y: -5, boxShadow: "0 10px 20px rgba(0,0,0,0.1)" }}
      transition={{ duration: 0.2 }}
      className="relative overflow-hidden rounded-xl shadow-md bg-white"
    >
      <a href="#" onClick={handleClick}>
        <div
          className="h-32 md:h-40 bg-center bg-cover"
          style={{ backgroundImage: `url(${imageUrl})` }}
        >
          <div className="absolute inset-0 bg-gradient-to-t from-black/60 to-transparent"></div>
        </div>
        <div className="absolute bottom-0 w-full p-3 text-white">
          <div className="flex items-center">
            {icon}
            <h3 className="ml-2 font-semibold">{name}</h3>
          </div>
        </div>
      </a>
    </motion.div>
  );
}
