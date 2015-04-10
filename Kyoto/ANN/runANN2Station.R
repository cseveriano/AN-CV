rm(list = ls(all = TRUE))

#install.packages('neuralnet')
#install.packages("hydroGOF")
#install.packages("matrixStats")


library("matrixStats")
library("neuralnet")
library("hydroGOF")
library(zoo)
library(forecast)
library(insol)


source("C:/Users/Carlos/Documents/Projetos Machine Learning/ANN-CV/CODES/Git/AN-CV/Kyoto/ANN/functions.R")


#dir0 = "C:/Users/Carlos/Dropbox/AN-CV/3 DATA/6 KYOTO/[619]/Converted to TXT/2013"
dirtrain = "C:/Users/Carlos/Documents/Projetos Machine Learning/ANN-CV/CODES/Git/AN-CV/Kyoto/Data/INPUTS/Delta/Train"
dirtest =  "C:/Users/Carlos/Documents/Projetos Machine Learning/ANN-CV/CODES/Git/AN-CV/Kyoto/Data/INPUTS/Delta/Test"
dir1 = "C:/Users/Carlos/Documents/Projetos Machine Learning/ANN-CV/CODES/Git/AN-CV/Kyoto/Data/OUTPUTS/ANN/Delta"

########################################################################
window_size = 4
data.interval = 30
########################################################################
setwd(dirtrain)
files = list.files(pattern='*\\[AVG-30\\].txt', recursive=TRUE)

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
AirP = data[,19] # 19 - Station 309

data = data.frame(Tm, G, AirP, zen)
data = data[complete.cases(data),] # remove missing points
#plot(data$G[1000:1048],type = "l")
#lines(data$CSky[1000:1048],col=2)
#data = data[which(data$zen<70),]

G = data$G

###NORMALIZATION ###
gs = sort(data$G, decreasing = TRUE)
ind = round(length(gs) * 0.05)
Gmax = gs[ind]
Gmin = gs[length(gs)]

d = (Gmax - Gmin) / 8
G = (G-(Gmin-d))/((Gmax +d)-(Gmin-d))
#G = (G-min(G))/(max(G)-min(G))

AirP = data$AirP
AirP = (AirP-min(AirP))/(max(AirP)-min(AirP))

input = NULL
output = NULL


for(i in 1:(length(G)-window_size))
{
  input = rbind(input, cbind(t(G[i:(i+window_size-1)]), t(AirP[i:(i+window_size-1)])))
  output = rbind(output, G[(i+window_size)])
}

data = data.frame(input, output)
data = data[complete.cases(data),] # remove missing points

traininginput <-  as.data.frame(input)
trainingoutput <- data$output

#Column bind the data into one variable
trainingdata <- cbind(traininginput,trainingoutput)
trainingdata = data[complete.cases(trainingdata),]

colnames(trainingdata) <- c("G1","G2","G3","G4","G5","G6","G7","G8","Output")

#Train the neural network
#Going to have 10 hidden layers
#Threshold is a numeric value specifying the threshold for the partial
#derivatives of the error function as stopping criteria.
net.sqrt <- neuralnet(Output~G1+G2+G3+G4+G5+G6+G7+G8,trainingdata, hidden=20, threshold=0.1, rep=3,
                      algorithm='rprop+',err.fct='sse', act.fct='tanh', linear.output=FALSE)
print(net.sqrt)

#Plot the neural network
#plot(net.sqrt)

########################################################################

setwd(dirtest)
files = list.files(pattern='*\\[AVG-30\\].txt', recursive=TRUE)

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
AirP = data[,19] # 19 - Station 309

data = data.frame(Tm, G, AirP, zen)
#plot(data$G[1000:1048],type = "l")
#lines(data$CSky[1000:1048],col=2)
#data = data[which(data$zen<70),]

G = data$G

### NORMALIZATION ###
gs = sort(data$G, decreasing = TRUE)
ind = round(length(gs) * 0.05)
Gmax = gs[ind]
Gmin = gs[length(gs)]

d = (Gmax - Gmin) / 8
G = (G-(Gmin-d))/((Gmax +d)-(Gmin-d))

AirP = data$AirP
AirP = (AirP-min(AirP))/(max(AirP)-min(AirP))

input = NULL
output = NULL


for(i in 1:(length(G)-window_size))
{
  input = rbind(input, cbind(t(G[i:(i+window_size-1)]), t(AirP[i:(i+window_size-1)])))
  output = rbind(output, G[(i+window_size)])
}

data = data.frame(input, output)
data = data[complete.cases(data),] # remove missing points

testinput <-  as.data.frame(input)
validateoutput <- output

########################################################################

#Test the neural network on some training data

net.results <- compute(net.sqrt, testinput) #Run them through the neural network

#Lets see what properties net.sqrt has
ls(net.results)

#Lets see the results
print(net.results$net.result)

print(nrmse(net.results$net.result[,1], validateoutput[,1]))