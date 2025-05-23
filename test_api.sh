#!/bin/bash

echo "Testing BGU Market API..."
echo "========================="

# Test 1: Login endpoint
echo "1. Testing login endpoint..."
response=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}')
echo "Response: $response"
echo ""

# Test 2: Register user endpoint
echo "2. Testing user registration..."
response=$(curl -s -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"testpass"}')
echo "Response: $response"
echo ""

# Test 3: Register guest endpoint
echo "3. Testing guest registration..."
response=$(curl -s -X POST http://localhost:8080/api/users/register/guest \
  -H "Content-Type: application/json" \
  -d '{"username":"guestuser"}')
echo "Response: $response"
echo ""

echo "API testing completed!" 