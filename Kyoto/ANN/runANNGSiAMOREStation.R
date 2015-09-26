rm(list = ls(all = TRUE))

#install.packages('neuralnet')
#install.packages("hydroGOF")
#install.packages("matrixStats")


library("matrixStats")
library("AMORE")
library("hydroGOF")
library(zoo)
library(forecast)
library(insol)
library(lubridate)


source("C:/Users/Carlos/Documents/Projetos Machine Learning/ANN-CV/CODES/Git/AN-CV/Kyoto/ANN/functions.R")


#dir0 = "C:/Users/Carlos/Dropbox/AN-CV/3 DATA/6 KYOTO/[619]/Converted to TXT/2013"
dirtrain = "C:/Users/Carlos/Documents/Projetos Machine Learning/ANN-CV/CODES/Git/AN-CV/Kyoto/Data/INPUTS/Delta/Train"
dirtest =  "C:/Users/Carlos/Documents/Projetos Machine Learning/ANN-CV/CODES/Git/AN-CV/Kyoto/Data/INPUTS/Delta/Test/2014-04"
dir1 = "C:/Users/Carlos/Documents/Projetos Machine Learning/ANN-CV/CODES/Git/AN-CV/Kyoto/Data/OUTPUTS/ANN/Delta"

########################################################################
window_size = 8
########################################################################
setwd(dirtrain)
files = list.files(pattern='*\\[AVG-60\\].txt', recursive=TRUE)

data = NULL
for(x in files)
{
  temp = read.table(x, header = TRUE, sep = "\t")
  data = rbind(data,temp)
  print(x)
}


Tm = as.POSIXlt(data$Tm, tz = "GMT")

G = data[,10] # GSi00 - 10 - Station 309
AirP = data[,19] # AirP - 19 - Station 309

data = data.frame(Tm, G, AirP)
#plot(data$G[1000:1048],type = "l")
#lines(data$CSky[1000:1048],col=2)
#data = data[which(data$zen<70),]

G = data$G
G = normalize(G)

AirP = data$AirP
AirP = normalize(AirP)

G_input = NULL
G_output = NULL


for(i in 1:(length(G)-window_size))
{
  G_input = rbind(G_input,cbind(hour(Tm[i]), t(G[i:(i+window_size-1)])))
  G_output = rbind(G_output, G[(i+window_size)])
}

G_input[,1] = normalize(G_input[,1])


data = data.frame(G_input, G_output)
data = data[complete.cases(data),] # remove missing points

traininginput <-  as.data.frame(G_input)
trainingoutput <- data$G_output

net <- newff(n.neurons=c((window_size)+1,6,1), learning.rate.global=1e-2, momentum.global=0.5,
             error.criterium="LMS", Stao=NA, hidden.layer="tansig", 
             output.layer="purelin", method="ADAPTgdwm")
result <- train(net, traininginput, trainingoutput, error.criterium="LMS", report=TRUE, show.step=100, n.shows=5)

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

Tm = as.POSIXlt(data$Tm, tz = "GMT")

G = data[,10] # 10 - Station 309
AirP = data[,19] # AirP - 19 - Station 309

data = data.frame(Tm, G, AirP)
#plot(data$G[1000:1048],type = "l")
#lines(data$CSky[1000:1048],col=2)
data = data[complete.cases(data),] # remove missing points
#data = data[which(data$zen<70),]

G = data$G
G = normalize(G)

AirP = data$AirP
AirP = normalize(AirP)

G_input = NULL
G_output = NULL


for(i in 1:(length(G)-window_size))
{
  G_input = rbind(G_input,cbind(hour(Tm[i]), t(G[i:(i+window_size-1)])))
  G_output = rbind(G_output, G[(i+window_size)])
}

G_input[,1] = normalize(G_input[,1])

data = data.frame(G_input, G_output)
data = data[complete.cases(data),] # remove missing points


testinput <-  as.data.frame(G_input)
validateoutput <- G_output

########################################################################

y <- sim(result$net, testinput)

print(nrmse(y[,1], validateoutput[,1]))