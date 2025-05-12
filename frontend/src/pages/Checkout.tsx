import React, { useState, useEffect } from "react";
import Header from "../components/Header";
import { User } from "@/entities/User";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group";
import { Separator } from "@/components/ui/separator";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { useToast } from "@/components/ui/use-toast";
import { CreditCard, ShieldCheck, BadgeCheck, CheckCircle2 } from "lucide-react";
import { useNavigate } from "react-router-dom";
import { createPageUrl } from "@/utils";

export default function Checkout() {
  const [cart, setCart] = useState([]);
  const [loading, setLoading] = useState(true);
  const [paymentMethod, setPaymentMethod] = useState("credit");
  const [shippingAddress, setShippingAddress] = useState({
    fullName: "",
    addressLine1: "",
    addressLine2: "",
    city: "",
    state: "",
    postalCode: "",
    country: "Israel"
  });
  const [billingAddress, setBillingAddress] = useState({
    fullName: "",
    addressLine1: "",
    addressLine2: "",
    city: "",
    state: "",
    postalCode: "",
    country: "Israel"
  });
  const [sameBillingAddress, setSameBillingAddress] = useState(true);
  const [cardDetails, setCardDetails] = useState({
    cardName: "",
    cardNumber: "",
    expiryDate: "",
    cvv: ""
  });
  const [processingPayment, setProcessingPayment] = useState(false);
  const { toast } = useToast();
  const navigate = useNavigate();

  useEffect(() => {
    loadCart();
  }, []);

  const loadCart = async () => {
    try {
      const user = await User.me();
      setCart(user.cart || []);
      
      // Pre-fill name if available
      if (user.full_name) {
        setShippingAddress(prev => ({
          ...prev,
          fullName: user.full_name
        }));
      }
    } catch (error) {
      console.error("Error loading cart:", error);
      toast({
        title: "Error",
        description: "Please sign in to proceed with checkout",
        variant: "destructive"
      });
      navigate(createPageUrl("Cart"));
    }
    setLoading(false);
  };

  const handleInputChange = (setter) => (e) => {
    const { name, value } = e.target;
    setter(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const calculateSubtotal = () => {
    return cart.reduce((total, item) => total + (item.price * item.quantity), 0);
  };

  const calculateTax = () => {
    return calculateSubtotal() * 0.17; // 17% VAT in Israel
  };

  const calculateShipping = () => {
    const subtotal = calculateSubtotal();
    if (subtotal > 100) return 0; // Free shipping for orders over $100
    return 10;
  };

  const calculateTotal = () => {
    return calculateSubtotal() + calculateTax() + calculateShipping();
  };

  const handleCheckout = async (e) => {
    e.preventDefault();
    setProcessingPayment(true);

    // Validate form fields
    if (!validateForm()) {
      setProcessingPayment(false);
      return;
    }

    // Simulate payment processing
    setTimeout(async () => {
      try {
        // Clear the cart
        await User.updateMyUserData({ cart: [] });
        
        // Show success toast
        toast({
          title: "Order placed successfully!",
          description: "Thank you for your purchase. You'll receive a confirmation email shortly.",
        });
        
        // Redirect to order confirmation
        navigate(createPageUrl("OrderConfirmation"));
      } catch (error) {
        toast({
          title: "Error processing payment",
          description: "Please try again or contact customer support.",
          variant: "destructive"
        });
        setProcessingPayment(false);
      }
    }, 2000);
  };

  const validateForm = () => {
    // Validate shipping address
    for (const [key, value] of Object.entries(shippingAddress)) {
      if (key !== 'addressLine2' && !value.trim()) {
        toast({
          title: "Missing information",
          description: `Please fill in the ${key.replace(/([A-Z])/g, ' $1').toLowerCase()} field.`,
          variant: "destructive"
        });
        return false;
      }
    }

    // Validate billing address if different
    if (!sameBillingAddress) {
      for (const [key, value] of Object.entries(billingAddress)) {
        if (key !== 'addressLine2' && !value.trim()) {
          toast({
            title: "Missing information",
            description: `Please fill in the billing ${key.replace(/([A-Z])/g, ' $1').toLowerCase()} field.`,
            variant: "destructive"
          });
          return false;
        }
      }
    }

    // Validate credit card details
    if (paymentMethod === "credit") {
      for (const [key, value] of Object.entries(cardDetails)) {
        if (!value.trim()) {
          toast({
            title: "Missing information",
            description: `Please fill in the ${key.replace(/([A-Z])/g, ' $1').toLowerCase()} field.`,
            variant: "destructive"
          });
          return false;
        }
      }

      // Simple validation for credit card number
      if (cardDetails.cardNumber.replace(/\s/g, '').length !== 16) {
        toast({
          title: "Invalid card number",
          description: "Please enter a valid 16-digit credit card number.",
          variant: "destructive"
        });
        return false;
      }

      // Simple validation for CVV
      if (cardDetails.cvv.length < 3 || cardDetails.cvv.length > 4) {
        toast({
          title: "Invalid CVV",
          description: "Please enter a valid CVV code.",
          variant: "destructive"
        });
        return false;
      }
    }

    return true;
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Header />
        <main className="container mx-auto px-4 py-8">
          <div className="animate-pulse">
            <div className="h-8 bg-gray-200 rounded w-1/4 mb-8"></div>
            <div className="space-y-4">
              {[1, 2, 3].map(i => (
                <div key={i} className="h-24 bg-gray-200 rounded"></div>
              ))}
            </div>
          </div>
        </main>
      </div>
    );
  }

  if (cart.length === 0) {
    navigate(createPageUrl("Cart"));
    return null;
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      <main className="container mx-auto px-4 py-8">
        <div className="max-w-6xl mx-auto">
          <h1 className="text-2xl font-bold mb-8">Checkout</h1>

          <div className="grid md:grid-cols-3 gap-8">
            <div className="md:col-span-2 space-y-6">
              {/* Shipping Information */}
              <div className="bg-white rounded-lg shadow p-6">
                <h2 className="text-xl font-semibold mb-4">Shipping Information</h2>
                <form className="space-y-4">
                  <div>
                    <Label htmlFor="fullName">Full Name</Label>
                    <Input
                      id="fullName"
                      name="fullName"
                      value={shippingAddress.fullName}
                      onChange={handleInputChange(setShippingAddress)}
                      placeholder="John Doe"
                    />
                  </div>

                  <div>
                    <Label htmlFor="addressLine1">Address Line 1</Label>
                    <Input
                      id="addressLine1"
                      name="addressLine1"
                      value={shippingAddress.addressLine1}
                      onChange={handleInputChange(setShippingAddress)}
                      placeholder="123 Street Name"
                    />
                  </div>

                  <div>
                    <Label htmlFor="addressLine2">Address Line 2 (Optional)</Label>
                    <Input
                      id="addressLine2"
                      name="addressLine2"
                      value={shippingAddress.addressLine2}
                      onChange={handleInputChange(setShippingAddress)}
                      placeholder="Apartment, suite, etc."
                    />
                  </div>

                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <Label htmlFor="city">City</Label>
                      <Input
                        id="city"
                        name="city"
                        value={shippingAddress.city}
                        onChange={handleInputChange(setShippingAddress)}
                        placeholder="Beer-Sheva"
                      />
                    </div>

                    <div>
                      <Label htmlFor="state">State/Province</Label>
                      <Input
                        id="state"
                        name="state"
                        value={shippingAddress.state}
                        onChange={handleInputChange(setShippingAddress)}
                        placeholder="South District"
                      />
                    </div>
                  </div>

                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <Label htmlFor="postalCode">Postal Code</Label>
                      <Input
                        id="postalCode"
                        name="postalCode"
                        value={shippingAddress.postalCode}
                        onChange={handleInputChange(setShippingAddress)}
                        placeholder="8410501"
                      />
                    </div>

                    <div>
                      <Label htmlFor="country">Country</Label>
                      <Input
                        id="country"
                        name="country"
                        value={shippingAddress.country}
                        onChange={handleInputChange(setShippingAddress)}
                        placeholder="Israel"
                      />
                    </div>
                  </div>

                  <div className="flex items-center space-x-2">
                    <input
                      type="checkbox"
                      id="sameBillingAddress"
                      checked={sameBillingAddress}
                      onChange={(e) => setSameBillingAddress(e.target.checked)}
                      className="rounded border-gray-300"
                    />
                    <Label htmlFor="sameBillingAddress">
                      Billing address is the same as shipping address
                    </Label>
                  </div>
                </form>
              </div>

              {/* Billing Information - Shown only if the checkbox is unchecked */}
              {!sameBillingAddress && (
                <div className="bg-white rounded-lg shadow p-6">
                  <h2 className="text-xl font-semibold mb-4">Billing Information</h2>
                  <form className="space-y-4">
                    <div>
                      <Label htmlFor="billingFullName">Full Name</Label>
                      <Input
                        id="billingFullName"
                        name="fullName"
                        value={billingAddress.fullName}
                        onChange={handleInputChange(setBillingAddress)}
                        placeholder="John Doe"
                      />
                    </div>

                    <div>
                      <Label htmlFor="billingAddressLine1">Address Line 1</Label>
                      <Input
                        id="billingAddressLine1"
                        name="addressLine1"
                        value={billingAddress.addressLine1}
                        onChange={handleInputChange(setBillingAddress)}
                        placeholder="123 Street Name"
                      />
                    </div>

                    <div>
                      <Label htmlFor="billingAddressLine2">Address Line 2 (Optional)</Label>
                      <Input
                        id="billingAddressLine2"
                        name="addressLine2"
                        value={billingAddress.addressLine2}
                        onChange={handleInputChange(setBillingAddress)}
                        placeholder="Apartment, suite, etc."
                      />
                    </div>

                    <div className="grid grid-cols-2 gap-4">
                      <div>
                        <Label htmlFor="billingCity">City</Label>
                        <Input
                          id="billingCity"
                          name="city"
                          value={billingAddress.city}
                          onChange={handleInputChange(setBillingAddress)}
                          placeholder="Beer-Sheva"
                        />
                      </div>

                      <div>
                        <Label htmlFor="billingState">State/Province</Label>
                        <Input
                          id="billingState"
                          name="state"
                          value={billingAddress.state}
                          onChange={handleInputChange(setBillingAddress)}
                          placeholder="South District"
                        />
                      </div>
                    </div>

                    <div className="grid grid-cols-2 gap-4">
                      <div>
                        <Label htmlFor="billingPostalCode">Postal Code</Label>
                        <Input
                          id="billingPostalCode"
                          name="postalCode"
                          value={billingAddress.postalCode}
                          onChange={handleInputChange(setBillingAddress)}
                          placeholder="8410501"
                        />
                      </div>

                      <div>
                        <Label htmlFor="billingCountry">Country</Label>
                        <Input
                          id="billingCountry"
                          name="country"
                          value={billingAddress.country}
                          onChange={handleInputChange(setBillingAddress)}
                          placeholder="Israel"
                        />
                      </div>
                    </div>
                  </form>
                </div>
              )}

              {/* Payment Method */}
              <div className="bg-white rounded-lg shadow p-6">
                <h2 className="text-xl font-semibold mb-4">Payment Method</h2>
                <RadioGroup value={paymentMethod} onValueChange={setPaymentMethod} className="space-y-3">
                  <div className="flex items-center space-x-2 border p-4 rounded-lg cursor-pointer hover:bg-gray-50">
                    <RadioGroupItem value="credit" id="credit" />
                    <Label htmlFor="credit" className="cursor-pointer flex items-center flex-1">
                      <CreditCard className="mr-2 h-5 w-5" />
                      Credit / Debit Card
                    </Label>
                    <div className="flex gap-2">
                      <img src="https://cdn-icons-png.flaticon.com/512/196/196578.png" alt="Visa" className="h-6" />
                      <img src="https://cdn-icons-png.flaticon.com/512/196/196561.png" alt="Mastercard" className="h-6" />
                      <img src="https://cdn-icons-png.flaticon.com/512/196/196539.png" alt="American Express" className="h-6" />
                    </div>
                  </div>

                  <div className="flex items-center space-x-2 border p-4 rounded-lg cursor-pointer hover:bg-gray-50">
                    <RadioGroupItem value="paypal" id="paypal" />
                    <Label htmlFor="paypal" className="cursor-pointer flex-1">
                      <img src="https://upload.wikimedia.org/wikipedia/commons/thumb/b/b5/PayPal.svg/1200px-PayPal.svg.png" alt="PayPal" className="h-6" />
                    </Label>
                  </div>
                </RadioGroup>

                {paymentMethod === "credit" && (
                  <div className="mt-4 space-y-4 pt-4 border-t">
                    <div>
                      <Label htmlFor="cardName">Cardholder Name</Label>
                      <Input
                        id="cardName"
                        name="cardName"
                        value={cardDetails.cardName}
                        onChange={handleInputChange(setCardDetails)}
                        placeholder="John Doe"
                      />
                    </div>

                    <div>
                      <Label htmlFor="cardNumber">Card Number</Label>
                      <Input
                        id="cardNumber"
                        name="cardNumber"
                        value={cardDetails.cardNumber}
                        onChange={handleInputChange(setCardDetails)}
                        placeholder="1234 5678 9012 3456"
                        maxLength={19}
                      />
                    </div>

                    <div className="grid grid-cols-2 gap-4">
                      <div>
                        <Label htmlFor="expiryDate">Expiry Date</Label>
                        <Input
                          id="expiryDate"
                          name="expiryDate"
                          value={cardDetails.expiryDate}
                          onChange={handleInputChange(setCardDetails)}
                          placeholder="MM/YY"
                          maxLength={5}
                        />
                      </div>

                      <div>
                        <Label htmlFor="cvv">CVV</Label>
                        <Input
                          id="cvv"
                          name="cvv"
                          value={cardDetails.cvv}
                          onChange={handleInputChange(setCardDetails)}
                          placeholder="123"
                          maxLength={4}
                          type="password"
                        />
                      </div>
                    </div>

                    <div className="flex items-center text-sm text-gray-500">
                      <ShieldCheck className="h-4 w-4 mr-2" />
                      Your payment information is secured with SSL encryption
                    </div>
                  </div>
                )}
              </div>
            </div>

            {/* Order Summary */}
            <div className="space-y-6">
              <div className="bg-white rounded-lg shadow p-6 sticky top-24">
                <h2 className="text-xl font-semibold mb-4">Order Summary</h2>
                
                <div className="space-y-4 max-h-64 overflow-y-auto mb-4">
                  {cart.map((item) => (
                    <div key={item.productId} className="flex items-center gap-3 border-b pb-2">
                      <img 
                        src={item.image} 
                        alt={item.title} 
                        className="w-12 h-12 object-cover rounded"
                      />
                      <div className="flex-grow">
                        <p className="font-medium line-clamp-1">{item.title}</p>
                        <div className="flex justify-between items-center mt-1">
                          <span className="text-sm text-gray-500">Qty: {item.quantity}</span>
                          <span>${(item.price * item.quantity).toFixed(2)}</span>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>

                <div className="space-y-2 border-t pt-4">
                  <div className="flex justify-between">
                    <span>Subtotal</span>
                    <span>${calculateSubtotal().toFixed(2)}</span>
                  </div>
                  <div className="flex justify-between">
                    <span>Shipping</span>
                    {calculateShipping() === 0 ? (
                      <span className="text-green-600">Free</span>
                    ) : (
                      <span>${calculateShipping().toFixed(2)}</span>
                    )}
                  </div>
                  <div className="flex justify-between">
                    <span>Tax (17% VAT)</span>
                    <span>${calculateTax().toFixed(2)}</span>
                  </div>
                  <Separator className="my-2" />
                  <div className="flex justify-between font-bold text-lg">
                    <span>Total</span>
                    <span>${calculateTotal().toFixed(2)}</span>
                  </div>

                  <div className="mt-6">
                    <Button
                      className="w-full bg-blue-600 hover:bg-blue-700"
                      size="lg"
                      onClick={handleCheckout}
                      disabled={processingPayment}
                    >
                      {processingPayment ? (
                        <>
                          <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-white mr-3"></div>
                          Processing...
                        </>
                      ) : (
                        "Complete Order"
                      )}
                    </Button>
                  </div>

                  <div className="text-center text-sm text-gray-500 mt-4">
                    <p className="flex items-center justify-center">
                      <ShieldCheck className="h-4 w-4 mr-1" />
                      Secure Checkout
                    </p>
                  </div>
                </div>
              </div>

              <div className="bg-white rounded-lg shadow p-4">
                <h3 className="font-semibold text-gray-700 mb-2">We Accept</h3>
                <div className="flex flex-wrap gap-2">
                  <img src="https://cdn-icons-png.flaticon.com/512/196/196578.png" alt="Visa" className="h-6" />
                  <img src="https://cdn-icons-png.flaticon.com/512/196/196561.png" alt="Mastercard" className="h-6" />
                  <img src="https://cdn-icons-png.flaticon.com/512/196/196539.png" alt="American Express" className="h-6" />
                  <img src="https://upload.wikimedia.org/wikipedia/commons/thumb/b/b5/PayPal.svg/1200px-PayPal.svg.png" alt="PayPal" className="h-6" />
                </div>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}