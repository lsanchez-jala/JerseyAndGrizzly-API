package product.management.Application;

import dagger.Binds;
import dagger.Module;
import product.management.Infrastructure.Repositories.*;
import product.management.Infrastructure.Repositories.impl.*;

@Module
public abstract class ApplicationAbstractModule {
    @Binds
    abstract IOrderRepository bindOrderRepo(OrderRepository impl);
    @Binds
    abstract IShipmentRepository bindShipmentRepo(ShipmentRepository impl);
    @Binds
    abstract IOrderItemRepository bindOrderItemRepo(OrderItemRepository impl);
    @Binds
    abstract IProductRepository bindProductRepo(ProductRepository impl);
    @Binds
    abstract ICustomerRepository bindCustomerRepo(CustomerRepository impl);
}

