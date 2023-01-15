package com.navaship.api.rates;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
public class RateService {
    private RateRepository rateRepository;
    private ModelMapper modelMapper;


    public RateService(RateRepository rateRepository, ModelMapper modelMapper) {
        this.rateRepository = rateRepository;
        this.modelMapper = modelMapper;
        // this.modelMapper.typeMap(Rate.class, NavaRate.class).addMapping(Rate::getId, NavaRate::setEasypostRateId);
        // this.modelMapper.addMappings(mapper -> mapper.skip(NavaRate::setId));
    }

    public Rate createRate(Rate rate) {
        return rateRepository.save(rate);
    }

    public Rate convertToNavaRate(com.easypost.model.Rate rate) {
        return modelMapper.map(rate, Rate.class);
    }
}
