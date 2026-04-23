package product.management.Application.impl;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import product.management.Application.IProductService;
import product.management.Application.exception.BadRequestException;
import product.management.Application.exception.ElementNotFoundException;
import product.management.Domain.DTO.Product.ProductDTO;
import product.management.Domain.DTO.Product.ProductRequest;
import product.management.Domain.Models.Product;
import product.management.Infrastructure.Mappers.ProductMapper;
import product.management.Infrastructure.Repositories.IProductRepository;

import java.util.List;
import java.util.UUID;

@Singleton
public class ProductService implements IProductService {

    private final IProductRepository repository;
    private final ProductMapper mapper;

    @Inject
    public ProductService(IProductRepository repository, ProductMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<ProductDTO> findAll(){
        return  repository.findAll().stream()
                .map(mapper::toDto)
                .toList();
    }

    public ProductDTO findById(UUID id){
        Product prod = repository.findById(id);
        if (prod == null){
            throw new ElementNotFoundException("Product with id: "+id+" was NOT FOUND.");
        }
        return mapper.toDto(prod);
    }

    public ProductDTO findBySku(String sku){
        Product prod = repository.findBySku(sku);
        if (prod == null){
            throw new ElementNotFoundException("Product with sku: "+sku+" was NOT FOUND.");
        }
        return mapper.toDto(prod);
    }

    public void delete(UUID id){
        this.findById(id);
        repository.delete(id);
    }

    public ProductDTO save(ProductRequest request){
        if (request == null){
            throw new BadRequestException("The request must not be empty");
        }
        if(request.name() == null || request.name().isEmpty()){
            throw new BadRequestException("The product's name must not be empty");
        }
        if(request.sku() == null || request.sku().isEmpty()){
            throw new BadRequestException("The product's sku must not be empty");
        }
        if(request.category() == null || request.category().isEmpty()){
            throw new BadRequestException("The product's category must not be empty");
        }
        if(request.price() == null ){
            throw new BadRequestException("The product's price must not be empty");
        }
        if(request.price() <= 0){
            throw new BadRequestException("The product's price must not ben equal or less than 0.");
        }
        if(request.stock() == null ){
            throw new BadRequestException("The product's stock must not be empty");
        }
        if(request.stock() <= 0){
            throw new BadRequestException("The product's stock must not ben equal or less than 0.");
        }
        if (repository.findBySku(request.sku()) != null){
            throw new BadRequestException("Product with sku: "+request.sku()+" already exists. The product sku must be unique.");
        }
        return mapper.toDto(repository.save(request));
    }

    public ProductDTO save(UUID id, ProductRequest request){
        this.findById(id);
        if(request.name() != null && request.name().isBlank()){
            throw new BadRequestException("The product's name must not be an empty string.");
        }
        if(request.sku() != null && repository.findBySku(request.sku()) != null){
            throw new BadRequestException("Product with sku: "+request.sku()+" already exists. The product sku must be unique.");
        }
        if(request.price() != null && request.price() <= 0){
            throw new BadRequestException("The product's price must not ben equal or less than 0.");
        }
        if(request.stock() != null && request.stock() <= 0){
            throw new BadRequestException("The product's stock must not ben equal or less than 0.");
        }
        if(request.category() != null && request.category().isBlank()){
            throw new BadRequestException("The product's category must not be an empty string.");
        }
        Product entity = repository.findById(id);
        mapper.toEntity(request, entity);
        return mapper.toDto(repository.save(id, entity));
    }
}
