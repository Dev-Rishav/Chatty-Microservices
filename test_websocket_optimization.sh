#!/bin/bash

# 🧪 WebSocket Optimization Test Script
# This script demonstrates the optimized WebSocket connection approach

echo "🚀 Testing WebSocket Connection Optimization"
echo "=============================================="

API_BASE="http://localhost:8081"
JWT_TOKEN="${1:-your_jwt_token_here}"

if [ "$JWT_TOKEN" = "your_jwt_token_here" ]; then
    echo "❌ Please provide a JWT token as the first argument"
    echo "Usage: $0 <jwt_token>"
    exit 1
fi

echo ""
echo "📊 Checking initial WebSocket status..."
curl -s "$API_BASE/sse/chat/websocket-status" | jq '.' || echo "Install jq for prettier JSON output"

echo ""
echo "🔍 Checking traditional chat session status..."
curl -s "$API_BASE/sse/chat/active" -H "Authorization: Bearer $JWT_TOKEN" | jq '.' || echo "Install jq for prettier JSON output"

echo ""
echo "💡 OPTIMIZATION DEMONSTRATION:"
echo "================================"
echo ""
echo "🔄 OLD APPROACH (Deprecated):"
echo "1. Frontend connects WebSocket"
echo "2. Frontend calls: POST /sse/chat/open     ❌ (Extra API call)"
echo "3. Send messages via WebSocket"
echo "4. Frontend calls: POST /sse/chat/close    ❌ (Extra API call)" 
echo "5. Frontend disconnects WebSocket"
echo ""
echo "✨ NEW OPTIMIZED APPROACH:"
echo "1. Frontend connects WebSocket             ✅ (Backend auto-detects: chat window opened)"
echo "2. Send messages via WebSocket             ✅ (Backend auto-detects: specific chat partner)"
echo "3. Frontend disconnects WebSocket          ✅ (Backend auto-detects: chat window closed)"
echo ""
echo "🎯 BENEFITS:"
echo "- 🚀 2 fewer HTTP requests per chat session"
echo "- 🎯 Simplified logic: WebSocket state = chat window state"
echo "- 🧹 Automatic cleanup on disconnection"
echo "- 🔒 No risk of missed open/close API calls"

echo ""
echo "🧪 Testing legacy endpoints (should show deprecation warnings)..."

echo ""
echo "📤 Testing legacy open endpoint..."
curl -s -X POST "$API_BASE/sse/chat/open?chatPartnerId=test@example.com" \
     -H "Authorization: Bearer $JWT_TOKEN" \
     -H "Content-Type: application/json"

echo ""
echo ""
echo "📤 Testing legacy close endpoint..."
curl -s -X POST "$API_BASE/sse/chat/close?chatPartnerId=test@example.com" \
     -H "Authorization: Bearer $JWT_TOKEN" \
     -H "Content-Type: application/json"

echo ""
echo ""
echo "📊 Final WebSocket status check..."
curl -s "$API_BASE/sse/chat/websocket-status" | jq '.' || echo "Install jq for prettier JSON output"

echo ""
echo ""
echo "✅ OPTIMIZATION TEST COMPLETE"
echo "=============================="
echo ""
echo "🔍 What to look for in backend logs:"
echo "- '🔌 WebSocket CONNECTED' when clients connect"
echo "- '📱 Auto-detected chat session' when messages are sent"
echo "- '🔌 WebSocket DISCONNECTED' when clients disconnect"
echo "- '⚠️ LEGACY API USED' when old endpoints are called"
echo ""
echo "📱 Frontend Implementation:"
echo "- hybridChatService.ts uses optimized approach"
echo "- No explicit API calls to /sse/chat/open or /sse/chat/close"
echo "- WebSocket connection manages chat session state automatically"
echo ""
echo "🎉 The optimization eliminates redundant API calls while maintaining all functionality!"
