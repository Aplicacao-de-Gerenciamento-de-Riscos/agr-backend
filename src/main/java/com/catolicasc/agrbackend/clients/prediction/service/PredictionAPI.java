package com.catolicasc.agrbackend.clients.prediction.service;

import com.catolicasc.agrbackend.clients.prediction.dto.PredictVersionDelayDTO;
import com.catolicasc.agrbackend.config.feignclient.FeignPredictionConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "prediction", url = "${PREDICTION_API_URL}", configuration = FeignPredictionConfiguration.class)
public interface PredictionAPI {
    @GetMapping(value = "predict-delay")
    List<PredictVersionDelayDTO> predictDelay(@RequestParam("version_ids")String versionIds);
}
