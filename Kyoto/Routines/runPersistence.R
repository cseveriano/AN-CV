#Clear all workspace
rm(list = ls(all = TRUE))
library(zoo)
library(forecast)
library(insol)

source("/Volumes/Macintosh Research/Others/AN/Codes/Thunderstorm/functions.R")
dir0 = "/Volumes/Macintosh Research/Others/AN/Data/Separate Items/GSi00"
dir1 = "/Volumes/Macintosh Research/Others/AN/Data"

########################################################################
##set globle parameters
train.length = 100#50
forecast.start = "2013-01-01"
data.interval = 15
########################################################################

setwd(dir0)
files = dir()

data = NULL
for(x in files)
{
  setwd(paste(dir0, x, sep = "/"))
  y = dir()[pmatch(paste(x,"[AVG-15]",sep = ""),dir())]
  setwd(paste(dir0,x,y,sep = "/"))
  files.day = dir()
  for(z in files.day)
  {
    temp = read.table(z, header = TRUE, sep = "\t")
    data = rbind(data,temp)
  }
  print(x)
}

SolPos = calZen(as.POSIXct(data$Tm, tz = "GMT")+ data.interval/2*60, lat = 1.3005, lon = 103.771, tz = 8, interval = data.interval)
zen = SolPos[["zenith"]]
CSky = SolPos[["clearsky"]]

Tm = as.POSIXlt(data$Tm, tz = "GMT")
G = data[,3]

data = data.frame(Tm, G, zen, CSky)
#plot(data$G[1000:1048],type = "l")
#lines(data$CSky[1000:1048],col=2)
data = data[complete.cases(data),] # remove missing points
data = data[which(data$zen<70),]
data$CIndex = data$G/data$CSky
Tm = as.character(trunc(data$Tm,"days"))
flag = which(Tm==forecast.start)[1]
ts = data$CIndex
data$forecast = rep(NA,nrow(data))
for(i in 1:(nrow(data)-flag+1))
{
  train = ts[((i+flag-1)-train.length):((i+flag-1)-1)]
  fit = auto.arima(train, trace = TRUE)
  data$forecast[i+flag-1] = forecast(fit,h=1)$mean[1]
  print(i)
}

data$forecast.GHI = data$forecast*data$CSky
data.out = data[flag:nrow(data),]
setwd(dir1)
write.table(data.out, "forecast2013Results.15min.SERIS.csv", sep = ",", row.names = FALSE)










