rm(list = ls(all = TRUE))

source("C:/Users/Carlos/Documents/Projetos Machine Learning/ANN-CV/CODES/Git/AN-CV/Kyoto/Routines/Persistence.R")
dir0 = "C:/Users/Carlos/Documents/Projetos Machine Learning/ANN-CV/CODES/Git/AN-CV/Kyoto/Data/Retroativo"
dir1 = "C:/Users/Carlos/Documents/Projetos Machine Learning/ANN-CV/CODES/Git/AN-CV/Kyoto/Data/OUTPUTS/PERSISTENCE/Retroativo"

########################################################################
##set globle parameters
forecast.start = "2013-12-01"
data.interval = 15
########################################################################

setwd(dir0)
files = dir()

data = NULL
for(x in files)
{
  #  setwd(paste(dir0, x, sep = "/"))
  #  y = dir()[pmatch(paste(x,"[AVG-15]",sep = ""),dir())]
  #  setwd(paste(dir0,x,y,sep = "/"))
  #  files.day = dir()
  #  for(z in files.day)
  #  {
  temp = read.table(x, header = TRUE, sep = "\t")
  data = rbind(data,temp)
  #  }
  print(x)
}

#SolPos = calZen(as.POSIXct(data$Tm, tz = "GMT")+ data.interval/2*60, lat = 1.3005, lon = 103.771, tz = 8, interval = data.interval)
#zen = SolPos[["zenith"]]
#CSky = SolPos[["clearsky"]]

Tm = as.POSIXlt(data$Tm, tz = "GMT")
G = data[,2]

data = data.frame(Tm, G)
#plot(data$G[1000:1048],type = "l")
#lines(data$CSky[1000:1048],col=2)
data = data[complete.cases(data),] # remove missing points
Tm = as.character(trunc(data$Tm,"days"))
flag = which(Tm==forecast.start)[1]
ts = data$G
data$forecast = rep(NA,nrow(data))
data.out = NULL
for(i in 1:(nrow(data)-flag+1))
{
  resultPer = Persistence(data[i+flag-1,1], data[i+flag-1,2])
  data.out = rbind(data.out,resultPer)
  print(i)
}

#data.out = cbind(substr(data[flag:nrow(data),1],1, 16), data[flag:nrow(data), 2],data$forecast[flag:nrow(data)])
setwd(dir1)
write.table(data.out, "[Kyoto]2013-12[FRCST-PRSTC-AVG-30].txt", sep = "\t", row.names = FALSE, quote=FALSE, col.names=c("Tm","ClearSky","GSi00","Kt","ClearSky_Next","Persistence"))
