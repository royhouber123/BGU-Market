import React, { useState } from "react";
import { User } from "@/entities/User";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { LogIn, UserPlus, ArrowLeft } from "lucide-react";

export default function AuthDialog({ open, onOpenChange }) {
  const [isLogin, setIsLogin] = useState(true);
  const [formData, setFormData] = useState({
    username: "",
    password: "",
    fullName: "",
    email: "",
    confirmPassword: ""
  });

  const handleInputChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  const handleGoogleLogin = async () => {
    try {
      await User.login();  // This will redirect to Google login
    } catch (error) {
      console.error("Login failed:", error);
    }
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    
    // In a real app, you would handle login/register here
    // For now we'll just use Google login as that's what's available
    
    console.log("Form submitted:", formData);
    handleGoogleLogin();
  };

  const handleToggleForm = () => {
    setIsLogin(!isLogin);
    // Reset form data when switching between login and register
    setFormData({
      username: "",
      password: "",
      fullName: "",
      email: "",
      confirmPassword: ""
    });
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle className="flex items-center">
            {!isLogin && (
              <Button 
                variant="ghost" 
                size="icon" 
                className="mr-2" 
                onClick={handleToggleForm}
              >
                <ArrowLeft className="h-4 w-4" />
              </Button>
            )}
            {isLogin ? "Sign in to Marketplace" : "Create an Account"}
          </DialogTitle>
          <DialogDescription>
            {isLogin ? "Enter your credentials to continue" : "Fill in the information below to create your account"}
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="space-y-4 py-4">
          {!isLogin && (
            <>
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="fullName">Full Name</Label>
                  <Input
                    id="fullName"
                    name="fullName"
                    value={formData.fullName}
                    onChange={handleInputChange}
                    required
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="email">Email</Label>
                  <Input
                    id="email"
                    name="email"
                    type="email"
                    value={formData.email}
                    onChange={handleInputChange}
                    required
                  />
                </div>
              </div>
            </>
          )}

          <div className="space-y-2">
            <Label htmlFor="username">Username</Label>
            <Input
              id="username"
              name="username"
              value={formData.username}
              onChange={handleInputChange}
              required
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="password">Password</Label>
            <Input
              id="password"
              name="password"
              type="password"
              value={formData.password}
              onChange={handleInputChange}
              required
            />
          </div>

          {!isLogin && (
            <div className="space-y-2">
              <Label htmlFor="confirmPassword">Confirm Password</Label>
              <Input
                id="confirmPassword"
                name="confirmPassword"
                type="password"
                value={formData.confirmPassword}
                onChange={handleInputChange}
                required
              />
            </div>
          )}

          <Button type="submit" className="w-full">
            {isLogin ? "Sign In" : "Create Account"}
          </Button>
        </form>

        <div className="relative">
          <div className="absolute inset-0 flex items-center">
            <span className="w-full border-t"></span>
          </div>
          <div className="relative flex justify-center text-xs uppercase">
            <span className="bg-white px-2 text-gray-500">Or continue with</span>
          </div>
        </div>

        <Button
          className="w-full mt-4 flex items-center justify-center gap-2"
          onClick={handleGoogleLogin}
          variant="outline"
        >
          <LogIn className="w-4 h-4" />
          Sign in with Google
        </Button>

        <DialogFooter className="mt-4 text-sm text-center">
          {isLogin ? (
            <p className="text-center w-full">
              Don't have an account?{" "}
              <button
                type="button"
                className="text-blue-600 hover:underline font-medium"
                onClick={handleToggleForm}
              >
                Register
              </button>
            </p>
          ) : (
            <p className="text-center w-full">
              Already have an account?{" "}
              <button
                type="button"
                className="text-blue-600 hover:underline font-medium"
                onClick={handleToggleForm}
              >
                Sign in
              </button>
            </p>
          )}
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}