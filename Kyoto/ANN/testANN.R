library("matrixStats")
library("AMORE")
library("hydroGOF")
library(zoo)
library(forecast)
library(insol)


source("C:/Users/Carlos/Documents/Projetos Machine Learning/ANN-CV/CODES/Git/AN-CV/Kyoto/ANN/functions.R")


#dir0 = "C:/Users/Carlos/Dropbox/AN-CV/3 DATA/6 KYOTO/[619]/Converted to TXT/2013"
dirtrain = "C:/Users/Carlos/Documents/Projetos Machine Learning/ANN-CV/CODES/Git/AN-CV/Kyoto/Data/INPUTS/Delta/Train"
dirtest =  "C:/Users/Carlos/Documents/Projetos Machine Learning/ANN-CV/CODES/Git/AN-CV/Kyoto/Data/INPUTS/Delta/Test/2015-03"
dir1 = "C:/Users/Carlos/Documents/Projetos Machine Learning/ANN-CV/CODES/Git/AN-CV/Kyoto/Data/OUTPUTS/ANN/Delta"

########################################################################
window_size = 8
data.interval = 30
########################################################################

setwd(dirtest)
files = list.files(pattern='*\\[AVG-60\\].txt', recursive=TRUE)

data = NULL
for(x in files)
{
  temp = read.table(x, header = TRUE, sep = "\t")
  data = rbind(data,temp)
  print(x)
}

SolPos = calZen(as.POSIXct(data$Tm, tz = "GMT")+ data.interval/2*60, lat = 1.3005, lon = 103.771, tz = 8, interval = data.interval)
zen = SolPos[["zenith"]]

Tm = as.POSIXlt(data$Tm, tz = "GMT")

G = data[,10] # 10 - Station 309
data = data.frame(Tm, G, zen)
#plot(data$G[1000:1048],type = "l")
#lines(data$CSky[1000:1048],col=2)
data = data[complete.cases(data),] # remove missing points
#data = data[which(data$zen<70),]

G = data$G
gs = sort(data$G, decreasing = TRUE)
ind = round(length(gs) * 0.05)
Gmax = gs[ind]
Gmin = gs[length(gs)]

d = (Gmax - Gmin) / 8
G = (G-(Gmin-d))/((Gmax +d)-(Gmin-d))

G_input = NULL
G_output = NULL


for(i in 1:(length(G)-window_size))
{
  G_input = rbind(G_input,cbind(hour(Tm[i]), t(G[i:(i+window_size-1)])))
  G_output = rbind(G_output, G[(i+window_size)])
}


Dmax = max(G_input[,1])
Dmin = min(G_input[,1])

d = (Dmax - Dmin) / 8
G_input[,1] = (G_input[,1]-(Dmin-d))/((Dmax +d)-(Dmin-d))


data = data.frame(G_input, G_output)
data = data[complete.cases(data),] # remove missing points


testinput <-  as.data.frame(G_input)
validateoutput <- G_output

########################################################################

y <- sim(result$net, testinput)

print(nrmse(y[,1], validateoutput[,1]))