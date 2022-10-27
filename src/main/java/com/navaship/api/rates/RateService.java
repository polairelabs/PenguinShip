package com.navaship.api.rates;

import com.easypost.model.Rate;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
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

    public NavaRate createRate(NavaRate rate) {
        return rateRepository.save(rate);
    }

    public NavaRate convertToNavaRate(Rate rate) {
        return modelMapper.map(rate, NavaRate.class);
    }
}
