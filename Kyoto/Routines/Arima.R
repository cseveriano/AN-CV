Arima <- function(gsiDate, gsi, timeseries){
  

  fit = auto.arima(train, trace = TRUE)
  result = forecast(fit,h=1)$mean[1]
  
  return (c(gsiDate,gsi, result ))
}







