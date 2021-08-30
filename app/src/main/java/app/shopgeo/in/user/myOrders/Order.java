package app.shopgeo.in.user.myOrders;

import app.shopgeo.in.databinding.OrderDetailFragmentBinding;

class Order {
    public static void setDeliveryStatus(OrderDetailFragmentBinding binding, String status){

        switch(status){
            case "Delivered" : {
                binding.delivered.setChecked(true);
            }
            case "OutForDelivery" : {
                binding.delivery.setChecked(true);
            }
            case "Shipped" :  {
                binding.shipped.setChecked(true);
            }
            case  "Shipping" : {
                binding.shipping.setChecked(true);
            }
            case "Dispatched" : {
                binding.dispatched.setChecked(true);
            }
            case "Preparing to Dispatch" : {
                binding.dispatching.setChecked(true);
            }
            default : {

            }
        }
    }
}
