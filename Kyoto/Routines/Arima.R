Arima <- function(gsiDate, gsi, timeseries){
  library(zoo)
  library(forecast)
  library(insol)

  fit = auto.arima(timeseries, trace = TRUE)

  result = forecast(fit,h=1)$mean[1]
  
  return (c(gsiDate,gsi, result ))
}







